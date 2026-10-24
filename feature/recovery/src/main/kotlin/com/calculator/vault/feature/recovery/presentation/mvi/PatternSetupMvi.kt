package com.calculator.vault.feature.recovery.presentation.mvi

import androidx.lifecycle.viewModelScope
import com.calculator.vault.core.ui.mvi.MviViewModel
import com.calculator.vault.core.ui.mvi.UiEffect
import com.calculator.vault.core.ui.mvi.UiIntent
import com.calculator.vault.core.ui.mvi.UiState
import com.calculator.vault.feature.recovery.domain.usecase.PatternTarget
import com.calculator.vault.feature.recovery.domain.usecase.SavePatternUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam

enum class PatternStep { DRAW, CONFIRM }

enum class PatternMessage { CONNECT_AT_LEAST, DRAW_AGAIN, TOO_SHORT, MISMATCH }

data class PatternSetupState(
    val step: PatternStep = PatternStep.DRAW,
    val message: PatternMessage = PatternMessage.CONNECT_AT_LEAST,
    val isError: Boolean = false,
    val resetKey: Int = 0,
    val saving: Boolean = false,
) : UiState

sealed interface PatternSetupIntent : UiIntent {
    data class Drawn(val pattern: List<Int>) : PatternSetupIntent
    data object Back : PatternSetupIntent
}

sealed interface PatternSetupEffect : UiEffect {
    data object Saved : PatternSetupEffect
    data object Close : PatternSetupEffect
}

@KoinViewModel
class PatternSetupViewModel(
    @InjectedParam private val target: PatternTarget,
    private val savePattern: SavePatternUseCase,
) : MviViewModel<PatternSetupState, PatternSetupIntent, PatternSetupEffect>(PatternSetupState()) {

    private var first: List<Int>? = null

    override fun handleIntent(intent: PatternSetupIntent) {
        when (intent) {
            PatternSetupIntent.Back -> sendEffect(PatternSetupEffect.Close)
            is PatternSetupIntent.Drawn -> onDrawn(intent.pattern)
        }
    }

    private fun onDrawn(pattern: List<Int>) {
        if (pattern.size < MIN_DOTS) {
            flashError(PatternMessage.TOO_SHORT)
            return
        }
        when (currentState.step) {
            PatternStep.DRAW -> {
                first = pattern
                setState { copy(step = PatternStep.CONFIRM, message = PatternMessage.DRAW_AGAIN, isError = false, resetKey = resetKey + 1) }
            }
            PatternStep.CONFIRM -> if (pattern != first) {
                first = null
                setState { copy(step = PatternStep.DRAW) }
                flashError(PatternMessage.MISMATCH)
            } else {
                setState { copy(saving = true) }
                viewModelScope.launch {
                    savePattern(target, pattern)
                        .onSuccess { sendEffect(PatternSetupEffect.Saved) }
                        .onFailure { setState { copy(saving = false) }; flashError(PatternMessage.MISMATCH) }
                }
            }
        }
    }

    private fun flashError(message: PatternMessage) {
        setState { copy(message = message, isError = true) }
        viewModelScope.launch {
            delay(900)
            setState { copy(isError = false, resetKey = resetKey + 1) }
        }
    }

    private companion object {
        const val MIN_DOTS = 4
    }
}
