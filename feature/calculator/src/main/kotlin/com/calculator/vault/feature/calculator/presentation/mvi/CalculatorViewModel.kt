package com.calculator.vault.feature.calculator.presentation.mvi

import androidx.lifecycle.viewModelScope
import com.calculator.vault.core.common.VaultConstants
import com.calculator.vault.core.designsystem.component.CalcKey
import com.calculator.vault.core.domain.repository.AuthRepository
import com.calculator.vault.core.domain.usecase.UnlockWithPinUseCase
import com.calculator.vault.core.model.PinCheckResult
import com.calculator.vault.core.model.RecoveryMethod
import com.calculator.vault.core.ui.mvi.MviViewModel
import com.calculator.vault.feature.calculator.domain.engine.CalcInput
import com.calculator.vault.feature.calculator.domain.engine.CalculatorEngine
import com.calculator.vault.feature.calculator.domain.usecase.ForgotPinPromptUseCase
import com.calculator.vault.feature.calculator.domain.usecase.RecordIntruderUseCase
import com.calculator.vault.feature.calculator.domain.usecase.ResetPinWithCodeUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import java.util.UUID

@KoinViewModel
class CalculatorViewModel(
    private val unlockWithPin: UnlockWithPinUseCase,
    private val auth: AuthRepository,
    private val recordIntruder: RecordIntruderUseCase,
    private val forgotPinPrompt: ForgotPinPromptUseCase,
    private val resetPinWithCode: ResetPinWithCodeUseCase,
) : MviViewModel<CalculatorState, CalculatorIntent, CalculatorEffect>(CalculatorState()) {

    /** One intruder-log entry per run of failed attempts on this screen. */
    private var burstId = UUID.randomUUID().toString()
    private var photoTaken = false

    init {
        auth.isBiometricUnlockEnabled
            .onEach { enabled -> setState { copy(fingerprintAvailable = enabled) } }
            .launchIn(viewModelScope)
    }

    override fun handleIntent(intent: CalculatorIntent) {
        when (intent) {
            is CalculatorIntent.Key -> onKey(intent.key)
            CalculatorIntent.EqualsLongPressed -> viewModelScope.launch { offerForgotPin(forgotPinPrompt.anyTime()) }
            CalculatorIntent.FingerprintClicked -> promptFingerprint(forReset = false)
            is CalculatorIntent.FingerprintSucceeded -> viewModelScope.launch {
                val cipher = intent.cipher ?: return@launch
                auth.unlockWithBiometric(cipher).onSuccess { if (intent.forReset) onUnlockedForReset() else onUnlocked() }
            }
            is CalculatorIntent.ForgotPinMethodChosen -> {
                setState { copy(forgotPinMethods = emptyList()) }
                if (intent.method == RecoveryMethod.FINGERPRINT) {
                    promptFingerprint(forReset = true)
                } else {
                    sendEffect(CalculatorEffect.OpenForgotPin(intent.method))
                }
            }
            CalculatorIntent.DismissForgotPin -> setState { copy(forgotPinMethods = emptyList()) }
            CalculatorIntent.BackPressed -> setState { copy(showExitDialog = true) }
            CalculatorIntent.DismissExit -> setState { copy(showExitDialog = false) }
            CalculatorIntent.ConfirmExit -> sendEffect(CalculatorEffect.ExitApp)
        }
    }

    private fun onKey(key: CalcKey) {
        if (currentState.unlocking) return
        val before = currentState.calc
        if (key == CalcKey.Equals) {
            if (CalculatorEngine.isCode(before, VaultConstants.RESET_PIN_CODE)) {
                resetWithCode()
                return
            }
            val candidate = CalculatorEngine.pinCandidate(before)
            if (candidate != null) {
                attemptUnlock(candidate)
                return
            }
        }
        setState { copy(calc = CalculatorEngine.reduce(calc, key.toInput())) }
    }

    private fun attemptUnlock(candidate: String) {
        setState { copy(unlocking = true) }
        viewModelScope.launch {
            when (val result = unlockWithPin(candidate)) {
                PinCheckResult.Success -> onUnlocked()
                is PinCheckResult.Wrong -> {
                    // Behave exactly like a calculator: "1234 =" just shows 1234.
                    setState { copy(unlocking = false, calc = CalculatorEngine.reduce(calc, CalcInput.EQUALS)) }
                    photoTaken = recordIntruder(burstId, result.consecutiveFailures, photoTaken)
                    offerForgotPin(forgotPinPrompt(result.consecutiveFailures))
                }
                PinCheckResult.NotAPin -> setState { copy(unlocking = false, calc = CalculatorEngine.reduce(calc, CalcInput.EQUALS)) }
            }
        }
    }

    /** Without a device-bound key (e.g. right after a reinstall) the code stays a plain number. */
    private fun resetWithCode() {
        setState { copy(unlocking = true) }
        viewModelScope.launch {
            resetPinWithCode()
                .onSuccess { onUnlockedForReset() }
                .onFailure { setState { copy(unlocking = false, calc = CalculatorEngine.reduce(calc, CalcInput.EQUALS)) } }
        }
    }

    private fun offerForgotPin(methods: List<RecoveryMethod>) {
        if (methods.isNotEmpty()) setState { copy(forgotPinMethods = methods) }
    }

    private fun promptFingerprint(forReset: Boolean) = viewModelScope.launch {
        val cipher = auth.biometricUnlockCipher()
        if (cipher == null) {
            auth.disableBiometricUnlock()
            sendEffect(CalculatorEffect.FingerprintUnavailable)
        } else {
            sendEffect(CalculatorEffect.PromptFingerprint(cipher, forReset))
        }
    }

    private fun onUnlocked() {
        resetScreen()
        sendEffect(CalculatorEffect.OpenVault)
    }

    private fun onUnlockedForReset() {
        resetScreen()
        sendEffect(CalculatorEffect.OpenResetPin)
    }

    private fun resetScreen() {
        burstId = UUID.randomUUID().toString()
        photoTaken = false
        setState { CalculatorState(fingerprintAvailable = fingerprintAvailable) }
    }

    private fun CalcKey.toInput(): CalcInput = when (this) {
        CalcKey.Clear -> CalcInput.CLEAR
        CalcKey.Negate -> CalcInput.NEGATE
        CalcKey.Percent -> CalcInput.PERCENT
        CalcKey.Divide -> CalcInput.DIVIDE
        CalcKey.Multiply -> CalcInput.MULTIPLY
        CalcKey.Subtract -> CalcInput.SUBTRACT
        CalcKey.Add -> CalcInput.ADD
        CalcKey.Decimal -> CalcInput.DECIMAL
        CalcKey.Backspace -> CalcInput.BACKSPACE
        CalcKey.Equals -> CalcInput.EQUALS
        else -> CalcInput.entries[checkNotNull(digit)]
    }
}
