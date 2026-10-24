package com.calculator.vault.feature.pin.presentation.mvi

import com.calculator.vault.core.designsystem.component.CalcKey
import com.calculator.vault.core.ui.mvi.UiEffect
import com.calculator.vault.core.ui.mvi.UiIntent
import com.calculator.vault.core.ui.mvi.UiState

enum class CreatePinStep { ENTER, CONFIRM }

data class CreatePinState(
    val step: CreatePinStep = CreatePinStep.ENTER,
    val input: String = "",
    val mismatch: Boolean = false,
    val errorTrigger: Int = 0,
    val creating: Boolean = false,
) : UiState {
    val isComplete: Boolean get() = input.length == PIN_LENGTH

    companion object {
        const val PIN_LENGTH = 4
    }
}

sealed interface CreatePinIntent : UiIntent {
    data class Key(val key: CalcKey) : CreatePinIntent
    data object ResetPassword : CreatePinIntent
}

sealed interface CreatePinEffect : UiEffect {
    data object Created : CreatePinEffect
    data object Failed : CreatePinEffect
}
