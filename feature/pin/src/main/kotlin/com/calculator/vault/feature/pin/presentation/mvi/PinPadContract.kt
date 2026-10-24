package com.calculator.vault.feature.pin.presentation.mvi

import com.calculator.vault.core.ui.mvi.UiEffect
import com.calculator.vault.core.ui.mvi.UiIntent
import com.calculator.vault.core.ui.mvi.UiState

enum class PinPadStep { CURRENT, NEW, CONFIRM }

enum class PinPadMode {
    /** Settings → Change PIN: current → new → confirm. */
    CHANGE,

    /** After a recovery method unlocked the vault: new → confirm. */
    RESET,
}

data class PinPadState(
    val mode: PinPadMode = PinPadMode.CHANGE,
    val step: PinPadStep = PinPadStep.CURRENT,
    val input: String = "",
    val error: PinPadError? = null,
    val errorTrigger: Int = 0,
    val busy: Boolean = false,
) : UiState

enum class PinPadError { WRONG_CURRENT, MISMATCH, SAVE_FAILED }

sealed interface PinPadIntent : UiIntent {
    data class Digit(val digit: Int) : PinPadIntent
    data object Backspace : PinPadIntent
    data object Close : PinPadIntent
}

sealed interface PinPadEffect : UiEffect {
    data object Done : PinPadEffect
    data object Close : PinPadEffect
}
