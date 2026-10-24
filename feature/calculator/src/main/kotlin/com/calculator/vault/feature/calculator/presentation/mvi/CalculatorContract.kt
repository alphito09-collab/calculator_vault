package com.calculator.vault.feature.calculator.presentation.mvi

import com.calculator.vault.core.designsystem.component.CalcKey
import com.calculator.vault.core.model.RecoveryMethod
import com.calculator.vault.core.ui.mvi.UiEffect
import com.calculator.vault.core.ui.mvi.UiIntent
import com.calculator.vault.core.ui.mvi.UiState
import com.calculator.vault.feature.calculator.domain.engine.CalcState
import javax.crypto.Cipher

data class CalculatorState(
    val calc: CalcState = CalcState(),
    val fingerprintAvailable: Boolean = false,
    val unlocking: Boolean = false,
    val showExitDialog: Boolean = false,
    /** Recovery methods offered by the "Forgot PIN?" dialog; empty while it is hidden. */
    val forgotPinMethods: List<RecoveryMethod> = emptyList(),
) : UiState

sealed interface CalculatorIntent : UiIntent {
    data class Key(val key: CalcKey) : CalculatorIntent
    data object EqualsLongPressed : CalculatorIntent
    data object FingerprintClicked : CalculatorIntent
    data class FingerprintSucceeded(val cipher: Cipher?, val forReset: Boolean) : CalculatorIntent
    data class ForgotPinMethodChosen(val method: RecoveryMethod) : CalculatorIntent
    data object DismissForgotPin : CalculatorIntent
    data object BackPressed : CalculatorIntent
    data object DismissExit : CalculatorIntent
    data object ConfirmExit : CalculatorIntent
}

sealed interface CalculatorEffect : UiEffect {
    data object OpenVault : CalculatorEffect
    data class OpenForgotPin(val method: RecoveryMethod) : CalculatorEffect

    /** The vault is open without the PIN (recovery or reset code): ask for a new one. */
    data object OpenResetPin : CalculatorEffect

    /** [forReset] verifies a "Forgot PIN?" fingerprint; the user then sets a new PIN. */
    data class PromptFingerprint(val cipher: Cipher, val forReset: Boolean) : CalculatorEffect
    data object FingerprintUnavailable : CalculatorEffect
    data object ExitApp : CalculatorEffect
}
