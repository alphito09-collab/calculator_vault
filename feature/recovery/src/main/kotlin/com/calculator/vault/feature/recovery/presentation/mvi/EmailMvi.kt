package com.calculator.vault.feature.recovery.presentation.mvi

import androidx.lifecycle.viewModelScope
import com.calculator.vault.core.domain.usecase.ValidateEmailUseCase
import com.calculator.vault.core.navigation.OtpPurpose
import com.calculator.vault.core.ui.mvi.MviViewModel
import com.calculator.vault.core.ui.mvi.UiEffect
import com.calculator.vault.core.ui.mvi.UiIntent
import com.calculator.vault.core.ui.mvi.UiState
import com.calculator.vault.feature.recovery.domain.usecase.CompleteEmailRecoveryUseCase
import com.calculator.vault.feature.recovery.domain.usecase.SendOtpUseCase
import com.calculator.vault.feature.recovery.domain.usecase.VerifyOtpUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam

enum class EmailError { OFFLINE, SEND_FAILED, WRONG_CODE, VERIFY_FAILED }

// region Email entry
data class RecoveryEmailState(
    val email: String = "",
    val isValid: Boolean = false,
    val sending: Boolean = false,
    val error: EmailError? = null,
) : UiState

sealed interface RecoveryEmailIntent : UiIntent {
    data class EmailChanged(val value: String) : RecoveryEmailIntent
    data object Continue : RecoveryEmailIntent
    data object Back : RecoveryEmailIntent
}

sealed interface RecoveryEmailEffect : UiEffect {
    data class CodeSent(val email: String) : RecoveryEmailEffect
    data object Close : RecoveryEmailEffect
}

@KoinViewModel
class RecoveryEmailViewModel(
    private val validate: ValidateEmailUseCase,
    private val sendOtp: SendOtpUseCase,
) : MviViewModel<RecoveryEmailState, RecoveryEmailIntent, RecoveryEmailEffect>(RecoveryEmailState()) {

    override fun handleIntent(intent: RecoveryEmailIntent) {
        when (intent) {
            is RecoveryEmailIntent.EmailChanged -> setState {
                copy(email = intent.value, isValid = validate(intent.value), error = null)
            }
            RecoveryEmailIntent.Back -> sendEffect(RecoveryEmailEffect.Close)
            RecoveryEmailIntent.Continue -> send()
        }
    }

    private fun send() {
        if (!currentState.isValid || currentState.sending) return
        setState { copy(sending = true, error = null) }
        viewModelScope.launch {
            val email = currentState.email.trim()
            when (sendOtp(email)) {
                SendOtpUseCase.Outcome.Sent -> sendEffect(RecoveryEmailEffect.CodeSent(email))
                SendOtpUseCase.Outcome.Offline -> setState { copy(error = EmailError.OFFLINE) }
                is SendOtpUseCase.Outcome.Failed -> setState { copy(error = EmailError.SEND_FAILED) }
            }
            setState { copy(sending = false) }
        }
    }
}
// endregion

// region OTP
data class EmailOtpState(
    val email: String,
    val purpose: OtpPurpose,
    val code: String = "",
    val verifying: Boolean = false,
    val resendIn: Int = RESEND_SECONDS,
    val error: EmailError? = null,
) : UiState {
    val canVerify: Boolean get() = code.length == CODE_LENGTH && !verifying

    companion object {
        const val CODE_LENGTH = 6
        const val RESEND_SECONDS = 60
    }
}

sealed interface EmailOtpIntent : UiIntent {
    data class CodeChanged(val value: String) : EmailOtpIntent
    data object Verify : EmailOtpIntent
    data object Resend : EmailOtpIntent
    data object Back : EmailOtpIntent
}

sealed interface EmailOtpEffect : UiEffect {
    data object EmailSaved : EmailOtpEffect
    data object VaultUnlocked : EmailOtpEffect
    data object Close : EmailOtpEffect
}

@KoinViewModel
class EmailOtpViewModel(
    @InjectedParam email: String,
    @InjectedParam purpose: OtpPurpose,
    private val verifyOtp: VerifyOtpUseCase,
    private val sendOtp: SendOtpUseCase,
    private val complete: CompleteEmailRecoveryUseCase,
) : MviViewModel<EmailOtpState, EmailOtpIntent, EmailOtpEffect>(EmailOtpState(email, purpose)) {

    private var countdown: Job? = null

    init {
        startCountdown()
    }

    override fun handleIntent(intent: EmailOtpIntent) {
        when (intent) {
            is EmailOtpIntent.CodeChanged -> {
                val digits = intent.value.filter(Char::isDigit).take(EmailOtpState.CODE_LENGTH)
                setState { copy(code = digits, error = null) }
                if (digits.length == EmailOtpState.CODE_LENGTH) verify()
            }
            EmailOtpIntent.Verify -> verify()
            EmailOtpIntent.Resend -> resend()
            EmailOtpIntent.Back -> sendEffect(EmailOtpEffect.Close)
        }
    }

    private fun verify() {
        if (!currentState.canVerify) return
        setState { copy(verifying = true, error = null) }
        viewModelScope.launch {
            verifyOtp(currentState.email, currentState.code)
                .onSuccess { valid ->
                    if (!valid) {
                        setState { copy(verifying = false, code = "", error = EmailError.WRONG_CODE) }
                        return@onSuccess
                    }
                    when (currentState.purpose) {
                        OtpPurpose.VERIFY_RECOVERY_EMAIL -> complete.saveEmail(currentState.email)
                            .onSuccess { sendEffect(EmailOtpEffect.EmailSaved) }
                        OtpPurpose.RESET_PIN -> complete.unlock()
                            .onSuccess { sendEffect(EmailOtpEffect.VaultUnlocked) }
                    }.onFailure { setState { copy(verifying = false, error = EmailError.VERIFY_FAILED) } }
                }
                .onFailure { error ->
                    val reason = if (error.message == "offline") EmailError.OFFLINE else EmailError.VERIFY_FAILED
                    setState { copy(verifying = false, error = reason) }
                }
        }
    }

    private fun resend() {
        if (currentState.resendIn > 0) return
        viewModelScope.launch {
            when (sendOtp(currentState.email)) {
                SendOtpUseCase.Outcome.Sent -> startCountdown()
                SendOtpUseCase.Outcome.Offline -> setState { copy(error = EmailError.OFFLINE) }
                is SendOtpUseCase.Outcome.Failed -> setState { copy(error = EmailError.SEND_FAILED) }
            }
        }
    }

    private fun startCountdown() {
        countdown?.cancel()
        countdown = viewModelScope.launch {
            for (left in EmailOtpState.RESEND_SECONDS downTo 0) {
                setState { copy(resendIn = left) }
                delay(1_000)
            }
        }
    }
}
// endregion
