package com.calculator.vault.feature.pin.presentation.mvi

import androidx.lifecycle.viewModelScope
import com.calculator.vault.core.domain.usecase.PinRules
import com.calculator.vault.core.ui.mvi.MviViewModel
import com.calculator.vault.feature.pin.domain.usecase.ChangePinUseCase
import com.calculator.vault.feature.pin.domain.usecase.ResetPinUseCase
import com.calculator.vault.feature.pin.domain.usecase.VerifyCurrentPinUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam

@KoinViewModel
class PinPadViewModel(
    @InjectedParam mode: PinPadMode,
    private val verifyCurrent: VerifyCurrentPinUseCase,
    private val changePin: ChangePinUseCase,
    private val resetPin: ResetPinUseCase,
) : MviViewModel<PinPadState, PinPadIntent, PinPadEffect>(
    PinPadState(mode = mode, step = if (mode == PinPadMode.RESET) PinPadStep.NEW else PinPadStep.CURRENT),
) {
    private var currentPin: String? = null
    private var newPin: String? = null

    override fun handleIntent(intent: PinPadIntent) {
        when (intent) {
            is PinPadIntent.Digit -> append(intent.digit)
            PinPadIntent.Backspace -> setState { copy(input = input.dropLast(1)) }
            PinPadIntent.Close -> sendEffect(PinPadEffect.Close)
        }
    }

    private fun append(digit: Int) {
        if (currentState.busy || currentState.input.length >= PinRules.LENGTH) return
        val input = currentState.input + digit
        setState { copy(input = input, error = if (input.length == 1) null else error) }
        if (input.length == PinRules.LENGTH) viewModelScope.launch { delay(120); submit(input) }
    }

    private suspend fun submit(pin: String) {
        when (currentState.step) {
            PinPadStep.CURRENT -> {
                setState { copy(busy = true) }
                if (verifyCurrent(pin)) {
                    currentPin = pin
                    setState { copy(step = PinPadStep.NEW, input = "", busy = false, error = null) }
                } else {
                    fail(PinPadError.WRONG_CURRENT)
                }
            }
            PinPadStep.NEW -> {
                newPin = pin
                setState { copy(step = PinPadStep.CONFIRM, input = "", error = null) }
            }
            PinPadStep.CONFIRM -> {
                if (pin != newPin) {
                    fail(PinPadError.MISMATCH)
                    return
                }
                setState { copy(busy = true) }
                val result = when (currentState.mode) {
                    PinPadMode.CHANGE -> changePin(checkNotNull(currentPin), pin)
                    PinPadMode.RESET -> resetPin(pin)
                }
                result
                    .onSuccess { sendEffect(PinPadEffect.Done) }
                    .onFailure { fail(PinPadError.SAVE_FAILED) }
            }
        }
    }

    private fun fail(error: PinPadError) =
        setState { copy(input = "", busy = false, error = error, errorTrigger = errorTrigger + 1) }
}
