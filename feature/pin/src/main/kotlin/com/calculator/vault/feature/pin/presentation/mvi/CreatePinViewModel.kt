package com.calculator.vault.feature.pin.presentation.mvi

import androidx.lifecycle.viewModelScope
import com.calculator.vault.core.designsystem.component.CalcKey
import com.calculator.vault.core.ui.mvi.MviViewModel
import com.calculator.vault.feature.pin.domain.usecase.CreateVaultUseCase
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

/** First-run PIN creation on the calculator keypad: type 4 digits, press "=", repeat. */
@KoinViewModel
class CreatePinViewModel(
    private val createVault: CreateVaultUseCase,
) : MviViewModel<CreatePinState, CreatePinIntent, CreatePinEffect>(CreatePinState()) {

    private var firstPin: String? = null

    override fun handleIntent(intent: CreatePinIntent) {
        when (intent) {
            is CreatePinIntent.Key -> onKey(intent.key)
            CreatePinIntent.ResetPassword -> {
                firstPin = null
                setState { CreatePinState() }
            }
        }
    }

    private fun onKey(key: CalcKey) {
        if (currentState.creating) return
        when {
            key.isDigit -> if (!currentState.isComplete) {
                setState { copy(input = input + key.label, mismatch = false) }
            }
            key == CalcKey.Backspace -> setState { copy(input = input.dropLast(1)) }
            key == CalcKey.Clear -> setState { copy(input = "", mismatch = false) }
            key == CalcKey.Equals -> onEquals()
            else -> Unit // Operators do nothing while choosing a PIN.
        }
    }

    private fun onEquals() {
        if (!currentState.isComplete) return
        val pin = currentState.input
        when (currentState.step) {
            CreatePinStep.ENTER -> {
                firstPin = pin
                setState { copy(step = CreatePinStep.CONFIRM, input = "") }
            }
            CreatePinStep.CONFIRM -> if (pin != firstPin) {
                setState { copy(input = "", mismatch = true, errorTrigger = errorTrigger + 1) }
            } else {
                setState { copy(creating = true) }
                viewModelScope.launch {
                    createVault(pin)
                        .onSuccess { sendEffect(CreatePinEffect.Created) }
                        .onFailure {
                            setState { copy(creating = false) }
                            sendEffect(CreatePinEffect.Failed)
                        }
                }
            }
        }
    }
}
