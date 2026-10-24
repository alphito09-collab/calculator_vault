package com.calculator.vault.feature.settings.presentation.mvi

import androidx.lifecycle.viewModelScope
import com.calculator.vault.core.model.AppIconOption
import com.calculator.vault.core.ui.mvi.MviViewModel
import com.calculator.vault.core.ui.mvi.UiEffect
import com.calculator.vault.core.ui.mvi.UiIntent
import com.calculator.vault.core.ui.mvi.UiState
import com.calculator.vault.feature.settings.domain.usecase.ChangeAppIconUseCase
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

data class AppIconState(
    val current: AppIconOption = AppIconOption.DEFAULT,
    val selected: AppIconOption = AppIconOption.DEFAULT,
    val confirm: Boolean = false,
) : UiState {
    val changed: Boolean get() = selected != current
}

sealed interface AppIconIntent : UiIntent {
    data class Select(val option: AppIconOption) : AppIconIntent
    data object Change : AppIconIntent
    data object Confirm : AppIconIntent
    data object Dismiss : AppIconIntent
    data object Back : AppIconIntent
}

sealed interface AppIconEffect : UiEffect {
    data object Changed : AppIconEffect
    data object Close : AppIconEffect
}

@KoinViewModel
class AppIconViewModel(private val changeIcon: ChangeAppIconUseCase) :
    MviViewModel<AppIconState, AppIconIntent, AppIconEffect>(AppIconState()) {

    init {
        val current = changeIcon.current()
        setState { copy(current = current, selected = current) }
    }

    override fun handleIntent(intent: AppIconIntent) {
        when (intent) {
            is AppIconIntent.Select -> setState { copy(selected = intent.option) }
            AppIconIntent.Change -> if (currentState.changed) setState { copy(confirm = true) }
            AppIconIntent.Dismiss -> setState { copy(confirm = false) }
            AppIconIntent.Confirm -> viewModelScope.launch {
                changeIcon(currentState.selected)
                setState { copy(confirm = false, current = selected) }
                sendEffect(AppIconEffect.Changed)
            }
            AppIconIntent.Back -> sendEffect(AppIconEffect.Close)
        }
    }
}
