package com.calculator.vault.core.ui.gallery

import androidx.lifecycle.viewModelScope
import com.calculator.vault.core.domain.repository.SettingsRepository
import com.calculator.vault.core.domain.usecase.DateSection
import com.calculator.vault.core.domain.usecase.DecryptForSharingUseCase
import com.calculator.vault.core.domain.usecase.GroupByDateUseCase
import com.calculator.vault.core.domain.usecase.MoveToTrashUseCase
import com.calculator.vault.core.domain.usecase.ObserveVaultItemsUseCase
import com.calculator.vault.core.domain.usecase.UnhideItemsUseCase
import com.calculator.vault.core.model.GridSize
import com.calculator.vault.core.model.MediaType
import com.calculator.vault.core.model.TransferProgress
import com.calculator.vault.core.model.VaultItem
import com.calculator.vault.core.ui.component.BulkAction
import com.calculator.vault.core.ui.mvi.MviViewModel
import com.calculator.vault.core.ui.mvi.UiEffect
import com.calculator.vault.core.ui.mvi.UiIntent
import com.calculator.vault.core.ui.mvi.UiState
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.File

data class GalleryState(
    val type: MediaType,
    val loading: Boolean = true,
    val items: List<VaultItem> = emptyList(),
    val sections: List<DateSection> = emptyList(),
    val selected: Set<String> = emptySet(),
    val gridSize: GridSize = GridSize.MEDIUM,
    val groupByDate: Boolean = true,
    val confirm: BulkAction? = null,
    val progress: TransferProgress? = null,
    val menuOpen: Boolean = false,
) : UiState {
    val selecting: Boolean get() = selected.isNotEmpty()
    val allSelected: Boolean get() = items.isNotEmpty() && selected.size == items.size
}

sealed interface GalleryIntent : UiIntent {
    data class Open(val item: VaultItem) : GalleryIntent
    data class Toggle(val item: VaultItem) : GalleryIntent
    data object ToggleSelectAll : GalleryIntent
    data object ClearSelection : GalleryIntent
    data object Share : GalleryIntent
    data object Delete : GalleryIntent
    data object Unhide : GalleryIntent
    data object Confirm : GalleryIntent
    data object DismissConfirm : GalleryIntent
    data object Import : GalleryIntent
    data object OpenMenu : GalleryIntent
    data object CloseMenu : GalleryIntent
    data class SetGridSize(val size: GridSize) : GalleryIntent
    data class SetGroupByDate(val enabled: Boolean) : GalleryIntent
    data object Back : GalleryIntent
}

sealed interface GalleryEffect : UiEffect {
    data class Open(val item: VaultItem) : GalleryEffect
    data class ShareFiles(val files: List<File>, val mimeType: String) : GalleryEffect
    data class Done(val action: BulkAction, val count: Int) : GalleryEffect
    data object Failed : GalleryEffect
    data object Import : GalleryEffect
    data object Back : GalleryEffect
}

/**
 * Shared logic for the photo/video grids and audio/file lists: selection, share, move to
 * trash, unhide and layout. Feature modules subclass it with `@KoinViewModel`.
 */
abstract class GalleryViewModel(
    type: MediaType,
    observeItems: ObserveVaultItemsUseCase,
    private val groupByDate: GroupByDateUseCase,
    private val settings: SettingsRepository,
    private val moveToTrash: MoveToTrashUseCase,
    private val unhideItems: UnhideItemsUseCase,
    private val decryptForSharing: DecryptForSharingUseCase,
) : MviViewModel<GalleryState, GalleryIntent, GalleryEffect>(GalleryState(type)) {

    init {
        combine(observeItems(type), settings.settings) { items, s -> items to s }
            .onEach { (items, s) ->
                setState {
                    copy(
                        loading = false,
                        items = items,
                        sections = groupByDate(items),
                        gridSize = s.gridSize,
                        groupByDate = s.groupByDate,
                        selected = selected.intersect(items.map { it.id }.toSet()),
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    override fun handleIntent(intent: GalleryIntent) {
        when (intent) {
            is GalleryIntent.Open -> if (currentState.selecting) toggle(intent.item) else sendEffect(GalleryEffect.Open(intent.item))
            is GalleryIntent.Toggle -> toggle(intent.item)
            GalleryIntent.ToggleSelectAll -> setState {
                copy(selected = if (allSelected) emptySet() else items.map { it.id }.toSet())
            }
            GalleryIntent.ClearSelection -> setState { copy(selected = emptySet()) }
            GalleryIntent.Share -> share()
            GalleryIntent.Delete -> setState { copy(confirm = BulkAction.Delete) }
            GalleryIntent.Unhide -> setState { copy(confirm = BulkAction.Unhide) }
            GalleryIntent.DismissConfirm -> setState { copy(confirm = null) }
            GalleryIntent.Confirm -> confirm()
            GalleryIntent.Import -> sendEffect(GalleryEffect.Import)
            GalleryIntent.OpenMenu -> setState { copy(menuOpen = true) }
            GalleryIntent.CloseMenu -> setState { copy(menuOpen = false) }
            is GalleryIntent.SetGridSize -> viewModelScope.launch { settings.setGridSize(intent.size) }
            is GalleryIntent.SetGroupByDate -> viewModelScope.launch { settings.setGroupByDate(intent.enabled) }
            GalleryIntent.Back -> if (currentState.selecting) setState { copy(selected = emptySet()) } else sendEffect(GalleryEffect.Back)
        }
    }

    private fun toggle(item: VaultItem) = setState {
        copy(selected = if (item.id in selected) selected - item.id else selected + item.id)
    }

    private fun share() {
        val ids = currentState.selected.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            decryptForSharing(ids)
                .onSuccess { files ->
                    val mime = currentState.items.filter { it.id in ids }.map { it.mimeType }.distinct().singleOrNull() ?: "*/*"
                    sendEffect(GalleryEffect.ShareFiles(files, mime))
                }
                .onFailure { sendEffect(GalleryEffect.Failed) }
        }
    }

    private fun confirm() {
        val action = currentState.confirm ?: return
        val ids = currentState.selected.toList()
        setState { copy(confirm = null) }
        viewModelScope.launch {
            when (action) {
                BulkAction.Delete -> moveToTrash(ids)
                    .onSuccess {
                        setState { copy(selected = emptySet()) }
                        sendEffect(GalleryEffect.Done(action, ids.size))
                    }
                    .onFailure { sendEffect(GalleryEffect.Failed) }
                BulkAction.Unhide -> {
                    var last = TransferProgress(0, ids.size)
                    setState { copy(progress = last) }
                    unhideItems(ids).onEach { last = it; setState { copy(progress = it) } }.collect()
                    setState { copy(progress = null, selected = emptySet()) }
                    sendEffect(GalleryEffect.Done(action, last.completed))
                }
                else -> Unit
            }
        }
    }
}
