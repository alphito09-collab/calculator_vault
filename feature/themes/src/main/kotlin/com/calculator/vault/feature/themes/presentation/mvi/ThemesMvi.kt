package com.calculator.vault.feature.themes.presentation.mvi

import androidx.lifecycle.viewModelScope
import com.calculator.vault.core.model.ThemeMode
import com.calculator.vault.core.ui.mvi.MviViewModel
import com.calculator.vault.core.ui.mvi.UiEffect
import com.calculator.vault.core.ui.mvi.UiIntent
import com.calculator.vault.core.ui.mvi.UiState
import com.calculator.vault.feature.themes.domain.usecase.ObserveThemeModeUseCase
import com.calculator.vault.feature.themes.domain.usecase.SetThemeModeUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

data class ThemesState(val mode: ThemeMode = ThemeMode.SYSTEM) : UiState

sealed interface ThemesIntent : UiIntent {
    data class Select(val mode: ThemeMode) : ThemesIntent
    data object Back : ThemesIntent
}

sealed interface ThemesEffect : UiEffect {
    data object Close : ThemesEffect
}

@KoinViewModel
class ThemesViewModel(
    observeMode: ObserveThemeModeUseCase,
    private val setMode: SetThemeModeUseCase,
) : MviViewModel<ThemesState, ThemesIntent, ThemesEffect>(ThemesState()) {

    init {
        observeMode().onEach { setState { copy(mode = it) } }.launchIn(viewModelScope)
    }

    override fun handleIntent(intent: ThemesIntent) {
        when (intent) {
            is ThemesIntent.Select -> viewModelScope.launch { setMode(intent.mode) }
            ThemesIntent.Back -> sendEffect(ThemesEffect.Close)
        }
    }
}
