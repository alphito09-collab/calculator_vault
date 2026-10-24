package com.calculator.vault.feature.trash.presentation.mvi

import androidx.lifecycle.viewModelScope
import com.calculator.vault.core.domain.usecase.DeletePermanentlyUseCase
import com.calculator.vault.core.domain.usecase.EmptyTrashUseCase
import com.calculator.vault.core.domain.usecase.ObserveTrashUseCase
import com.calculator.vault.core.domain.usecase.RestoreFromTrashUseCase
import com.calculator.vault.core.model.VaultItem
import com.calculator.vault.core.ui.component.BulkAction
import com.calculator.vault.core.ui.mvi.MviViewModel
import com.calculator.vault.core.ui.mvi.UiEffect
import com.calculator.vault.core.ui.mvi.UiIntent
import com.calculator.vault.core.ui.mvi.UiState
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

data class TrashState(
    val loading: Boolean = true,
    val items: List<VaultItem> = emptyList(),
    val selected: Set<String> = emptySet(),
    val confirm: BulkAction? = null,
    val confirmEmpty: Boolean = false,
) : UiState {
    val selecting: Boolean get() = selected.isNotEmpty()
}

sealed interface TrashIntent : UiIntent {
    data class Toggle(val item: VaultItem) : TrashIntent
    data object ToggleSelectAll : TrashIntent
    data object ClearSelection : TrashIntent
    data object Restore : TrashIntent
    data object DeleteForever : TrashIntent
    data object EmptyTrash : TrashIntent
    data object Confirm : TrashIntent
    data object Dismiss : TrashIntent
    data object Back : TrashIntent
}

sealed interface TrashEffect : UiEffect {
    data class Restored(val count: Int) : TrashEffect
    data class Deleted(val count: Int) : TrashEffect
    data object Failed : TrashEffect
    data object Close : TrashEffect
}

@KoinViewModel
class TrashViewModel(
    observeTrash: ObserveTrashUseCase,
    private val restore: RestoreFromTrashUseCase,
    private val deleteForever: DeletePermanentlyUseCase,
    private val emptyTrash: EmptyTrashUseCase,
) : MviViewModel<TrashState, TrashIntent, TrashEffect>(TrashState()) {

    init {
        observeTrash().onEach { items ->
            setState { copy(loading = false, items = items, selected = selected.intersect(items.map { it.id }.toSet())) }
        }.launchIn(viewModelScope)
    }

    override fun handleIntent(intent: TrashIntent) {
        when (intent) {
            is TrashIntent.Toggle -> setState {
                copy(selected = if (intent.item.id in selected) selected - intent.item.id else selected + intent.item.id)
            }
            TrashIntent.ToggleSelectAll -> setState {
                copy(selected = if (selected.size == items.size) emptySet() else items.map { it.id }.toSet())
            }
            TrashIntent.ClearSelection -> setState { copy(selected = emptySet()) }
            TrashIntent.Restore -> setState { copy(confirm = BulkAction.Restore) }
            TrashIntent.DeleteForever -> setState { copy(confirm = BulkAction.DeleteForever) }
            TrashIntent.EmptyTrash -> if (currentState.items.isNotEmpty()) setState { copy(confirmEmpty = true) }
            TrashIntent.Dismiss -> setState { copy(confirm = null, confirmEmpty = false) }
            TrashIntent.Confirm -> confirm()
            TrashIntent.Back -> if (currentState.selecting) setState { copy(selected = emptySet()) } else sendEffect(TrashEffect.Close)
        }
    }

    private fun confirm() {
        val ids = currentState.selected.toList()
        val emptyAll = currentState.confirmEmpty
        val action = currentState.confirm
        setState { copy(confirm = null, confirmEmpty = false) }
        viewModelScope.launch {
            when {
                emptyAll -> {
                    val count = currentState.items.size
                    emptyTrash().fold({ sendEffect(TrashEffect.Deleted(count)) }, { sendEffect(TrashEffect.Failed) })
                }
                action == BulkAction.Restore -> restore(ids).fold({ sendEffect(TrashEffect.Restored(ids.size)) }, { sendEffect(TrashEffect.Failed) })
                action == BulkAction.DeleteForever -> deleteForever(ids).fold({ sendEffect(TrashEffect.Deleted(ids.size)) }, { sendEffect(TrashEffect.Failed) })
            }
            setState { copy(selected = emptySet()) }
        }
    }
}
