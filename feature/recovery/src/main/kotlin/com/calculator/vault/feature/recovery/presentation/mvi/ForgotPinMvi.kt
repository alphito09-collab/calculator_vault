package com.calculator.vault.feature.recovery.presentation.mvi

import androidx.lifecycle.viewModelScope
import com.calculator.vault.core.model.RecoveryMethod
import com.calculator.vault.core.model.SecurityQuestion
import com.calculator.vault.core.ui.mvi.MviViewModel
import com.calculator.vault.core.ui.mvi.UiEffect
import com.calculator.vault.core.ui.mvi.UiIntent
import com.calculator.vault.core.ui.mvi.UiState
import com.calculator.vault.feature.recovery.domain.usecase.AvailableRecoveryMethodsUseCase
import com.calculator.vault.feature.recovery.domain.usecase.FingerprintRecoveryUseCase
import com.calculator.vault.feature.recovery.domain.usecase.ObserveRecoveryOptionsUseCase
import com.calculator.vault.feature.recovery.domain.usecase.SendOtpUseCase
import com.calculator.vault.feature.recovery.domain.usecase.UnlockWithAnswerUseCase
import com.calculator.vault.feature.recovery.domain.usecase.UnlockWithPatternUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import javax.crypto.Cipher

data class ForgotPinState(
    val loading: Boolean = true,
    val methods: List<RecoveryMethod> = emptyList(),
    val active: RecoveryMethod? = null,
    /** Opened for one method from the calculator dialog: back closes instead of showing the list. */
    val preselected: Boolean = false,
    val email: String? = null,
    val question: SecurityQuestion? = null,
    val answer: String = "",
    val patternError: Boolean = false,
    val patternResetKey: Int = 0,
    val wrongAnswer: Boolean = false,
    val busy: Boolean = false,
    val error: EmailError? = null,
) : UiState

sealed interface ForgotPinIntent : UiIntent {
    /** Sent once the screen is shown; [method] skips the method list. */
    data class Start(val method: RecoveryMethod?) : ForgotPinIntent
    data class Choose(val method: RecoveryMethod) : ForgotPinIntent
    data class PatternDrawn(val pattern: List<Int>) : ForgotPinIntent
    data class AnswerChanged(val value: String) : ForgotPinIntent
    data object SubmitAnswer : ForgotPinIntent
    data class FingerprintVerified(val cipher: Cipher?) : ForgotPinIntent
    data object Back : ForgotPinIntent
}

sealed interface ForgotPinEffect : UiEffect {
    data object Recovered : ForgotPinEffect
    data class PromptFingerprint(val cipher: Cipher) : ForgotPinEffect
    data class OtpSent(val email: String) : ForgotPinEffect
    data object Close : ForgotPinEffect
}

@KoinViewModel
class ForgotPinViewModel(
    private val availableMethods: AvailableRecoveryMethodsUseCase,
    private val options: ObserveRecoveryOptionsUseCase,
    private val fingerprint: FingerprintRecoveryUseCase,
    private val unlockWithPattern: UnlockWithPatternUseCase,
    private val unlockWithAnswer: UnlockWithAnswerUseCase,
    private val sendOtp: SendOtpUseCase,
) : MviViewModel<ForgotPinState, ForgotPinIntent, ForgotPinEffect>(ForgotPinState()) {

    private var started = false

    override fun handleIntent(intent: ForgotPinIntent) {
        when (intent) {
            is ForgotPinIntent.Start -> start(intent.method)
            is ForgotPinIntent.Choose -> choose(intent.method)
            is ForgotPinIntent.PatternDrawn -> attempt(
                onFail = {
                    setState { copy(patternError = true) }
                    viewModelScope.launch { delay(800); setState { copy(patternError = false, patternResetKey = patternResetKey + 1) } }
                },
            ) { unlockWithPattern(intent.pattern) }
            is ForgotPinIntent.AnswerChanged -> setState { copy(answer = intent.value, wrongAnswer = false) }
            ForgotPinIntent.SubmitAnswer -> attempt(onFail = { setState { copy(wrongAnswer = true) } }) { unlockWithAnswer(currentState.answer) }
            is ForgotPinIntent.FingerprintVerified -> {
                val cipher = intent.cipher ?: return
                attempt(onFail = {}) { fingerprint.unlock(cipher) }
            }
            ForgotPinIntent.Back ->
                if (currentState.active != null && currentState.methods.size > 1 && !currentState.preselected) setState { copy(active = null) }
                else sendEffect(ForgotPinEffect.Close)
        }
    }

    /** Runs once per screen (not again after a configuration change). */
    private fun start(method: RecoveryMethod?) {
        if (started) return
        started = true
        viewModelScope.launch {
            val opts = options().first()
            val methods = availableMethods()
            val preselected = method?.takeIf { it in methods }
            setState {
                copy(loading = false, methods = methods, email = opts.email, question = opts.securityQuestion, preselected = preselected != null)
            }
            preselected?.let(::choose)
        }
    }

    private fun choose(method: RecoveryMethod) {
        when (method) {
            RecoveryMethod.FINGERPRINT -> viewModelScope.launch {
                fingerprint.unlockCipher()?.let { sendEffect(ForgotPinEffect.PromptFingerprint(it)) }
            }
            RecoveryMethod.EMAIL -> {
                val email = currentState.email ?: return
                setState { copy(busy = true, error = null) }
                viewModelScope.launch {
                    when (sendOtp(email)) {
                        SendOtpUseCase.Outcome.Sent -> sendEffect(ForgotPinEffect.OtpSent(email))
                        SendOtpUseCase.Outcome.Offline -> setState { copy(error = EmailError.OFFLINE) }
                        is SendOtpUseCase.Outcome.Failed -> setState { copy(error = EmailError.SEND_FAILED) }
                    }
                    setState { copy(busy = false) }
                }
            }
            else -> setState { copy(active = method) }
        }
    }

    private fun attempt(onFail: () -> Unit, block: suspend () -> Result<Unit>) {
        if (currentState.busy) return
        setState { copy(busy = true) }
        viewModelScope.launch {
            block()
                .onSuccess { sendEffect(ForgotPinEffect.Recovered) }
                .onFailure { onFail() }
            setState { copy(busy = false) }
        }
    }
}
