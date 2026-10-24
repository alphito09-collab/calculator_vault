package com.calculator.vault.feature.photos.presentation.mvi

import androidx.lifecycle.viewModelScope
import com.calculator.vault.core.domain.usecase.DecryptForSharingUseCase
import com.calculator.vault.core.domain.usecase.MoveToTrashUseCase
import com.calculator.vault.core.domain.usecase.ObserveVaultItemsUseCase
import com.calculator.vault.core.domain.usecase.RenameItemUseCase
import com.calculator.vault.core.domain.usecase.UnhideItemsUseCase
import com.calculator.vault.core.model.MediaType
import com.calculator.vault.core.model.TransferProgress
import com.calculator.vault.core.model.VaultItem
import com.calculator.vault.core.ui.component.BulkAction
import com.calculator.vault.core.ui.mvi.MviViewModel
import com.calculator.vault.core.ui.mvi.UiEffect
import com.calculator.vault.core.ui.mvi.UiIntent
import com.calculator.vault.core.ui.mvi.UiState
import com.calculator.vault.feature.photos.domain.usecase.SlideshowIntervalUseCase
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam
import java.io.File

data class PhotoViewerState(
    val items: List<VaultItem> = emptyList(),
    val index: Int = -1,
    val chromeVisible: Boolean = true,
    val menuOpen: Boolean = false,
    val renaming: Boolean = false,
    val showDetails: Boolean = false,
    val pickingInterval: Boolean = false,
    val slideshow: Boolean = false,
    val intervalSeconds: Int = 2,
    val confirm: BulkAction? = null,
    val progress: TransferProgress? = null,
) : UiState {
    val current: VaultItem? get() = items.getOrNull(index)
}

sealed interface PhotoViewerIntent : UiIntent {
    data class PageChanged(val index: Int) : PhotoViewerIntent
    data object ToggleChrome : PhotoViewerIntent
    data object OpenMenu : PhotoViewerIntent
    data object CloseMenu : PhotoViewerIntent
    data object Rename : PhotoViewerIntent
    data class RenameConfirmed(val name: String) : PhotoViewerIntent
    data object Details : PhotoViewerIntent
    data object Slideshow : PhotoViewerIntent
    data class StartSlideshow(val seconds: Int) : PhotoViewerIntent
    data object StopSlideshow : PhotoViewerIntent
    data object DismissDialogs : PhotoViewerIntent
    data object Share : PhotoViewerIntent
    data object Delete : PhotoViewerIntent
    data object Unhide : PhotoViewerIntent
    data object Confirm : PhotoViewerIntent
    data object Back : PhotoViewerIntent
}

sealed interface PhotoViewerEffect : UiEffect {
    data class ShareFiles(val files: List<File>, val mimeType: String) : PhotoViewerEffect
    data class Done(val action: BulkAction) : PhotoViewerEffect
    data object Failed : PhotoViewerEffect
    data object Close : PhotoViewerEffect
}

@KoinViewModel
class PhotoViewerViewModel(
    @InjectedParam private val startId: String,
    @InjectedParam startSlideshow: Boolean,
    observeItems: ObserveVaultItemsUseCase,
    private val interval: SlideshowIntervalUseCase,
    private val rename: RenameItemUseCase,
    private val moveToTrash: MoveToTrashUseCase,
    private val unhide: UnhideItemsUseCase,
    private val decrypt: DecryptForSharingUseCase,
) : MviViewModel<PhotoViewerState, PhotoViewerIntent, PhotoViewerEffect>(PhotoViewerState(slideshow = startSlideshow, chromeVisible = !startSlideshow)) {

    init {
        observeItems(MediaType.PHOTO).onEach { items ->
            if (items.isEmpty()) sendEffect(PhotoViewerEffect.Close)
            setState {
                val keepId = current?.id ?: startId
                val newIndex = items.indexOfFirst { it.id == keepId }.takeIf { it >= 0 } ?: index.coerceIn(0, (items.size - 1).coerceAtLeast(0))
                copy(items = items, index = newIndex)
            }
        }.launchIn(viewModelScope)
        interval.observe().onEach { setState { copy(intervalSeconds = it) } }.launchIn(viewModelScope)
    }

    override fun handleIntent(intent: PhotoViewerIntent) {
        when (intent) {
            is PhotoViewerIntent.PageChanged -> setState { copy(index = intent.index) }
            PhotoViewerIntent.ToggleChrome -> setState {
                if (slideshow) copy(slideshow = false, chromeVisible = true) else copy(chromeVisible = !chromeVisible)
            }
            PhotoViewerIntent.OpenMenu -> setState { copy(menuOpen = true) }
            PhotoViewerIntent.CloseMenu -> setState { copy(menuOpen = false) }
            PhotoViewerIntent.Rename -> setState { copy(menuOpen = false, renaming = true) }
            is PhotoViewerIntent.RenameConfirmed -> viewModelScope.launch {
                val item = currentState.current ?: return@launch
                setState { copy(renaming = false) }
                rename(item, intent.name).onFailure { sendEffect(PhotoViewerEffect.Failed) }
            }
            PhotoViewerIntent.Details -> setState { copy(menuOpen = false, showDetails = true) }
            PhotoViewerIntent.Slideshow -> setState { copy(menuOpen = false, pickingInterval = true) }
            is PhotoViewerIntent.StartSlideshow -> viewModelScope.launch {
                interval.set(intent.seconds)
                setState { copy(pickingInterval = false, slideshow = true, chromeVisible = false, intervalSeconds = intent.seconds) }
            }
            PhotoViewerIntent.StopSlideshow -> setState { copy(slideshow = false, chromeVisible = true) }
            PhotoViewerIntent.DismissDialogs -> setState { copy(renaming = false, showDetails = false, pickingInterval = false, confirm = null) }
            PhotoViewerIntent.Share -> share()
            PhotoViewerIntent.Delete -> setState { copy(confirm = BulkAction.Delete) }
            PhotoViewerIntent.Unhide -> setState { copy(confirm = BulkAction.Unhide) }
            PhotoViewerIntent.Confirm -> confirm()
            PhotoViewerIntent.Back -> sendEffect(PhotoViewerEffect.Close)
        }
    }

    private fun share() {
        val item = currentState.current ?: return
        viewModelScope.launch {
            decrypt(listOf(item.id))
                .onSuccess { sendEffect(PhotoViewerEffect.ShareFiles(it, item.mimeType)) }
                .onFailure { sendEffect(PhotoViewerEffect.Failed) }
        }
    }

    private fun confirm() {
        val action = currentState.confirm ?: return
        val item = currentState.current ?: return
        setState { copy(confirm = null) }
        viewModelScope.launch {
            when (action) {
                BulkAction.Delete -> moveToTrash(listOf(item.id))
                    .onSuccess { sendEffect(PhotoViewerEffect.Done(action)) }
                    .onFailure { sendEffect(PhotoViewerEffect.Failed) }
                BulkAction.Unhide -> {
                    setState { copy(progress = TransferProgress(0, 1)) }
                    unhide(listOf(item.id)).onEach { setState { copy(progress = it) } }.collect()
                    setState { copy(progress = null) }
                    sendEffect(PhotoViewerEffect.Done(action))
                }
                else -> Unit
            }
        }
    }
}
