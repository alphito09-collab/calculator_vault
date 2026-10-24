package com.calculator.vault.feature.recovery.presentation.mvi

import androidx.lifecycle.viewModelScope
import com.calculator.vault.core.model.RecoveryOptions
import com.calculator.vault.core.ui.mvi.MviViewModel
import com.calculator.vault.core.ui.mvi.UiEffect
import com.calculator.vault.core.ui.mvi.UiIntent
import com.calculator.vault.core.ui.mvi.UiState
import com.calculator.vault.feature.recovery.domain.usecase.FingerprintRecoveryUseCase
import com.calculator.vault.feature.recovery.domain.usecase.FinishRecoveryPromptUseCase
import com.calculator.vault.feature.recovery.domain.usecase.ObserveRecoveryOptionsUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam
import javax.crypto.Cipher

data class RecoveryMethodState(
    val fromSettings: Boolean = false,
    val options: RecoveryOptions = RecoveryOptions(),
) : UiState

sealed interface RecoveryMethodIntent : UiIntent {
    data class FingerprintToggled(val enabled: Boolean) : RecoveryMethodIntent
    data class FingerprintVerified(val cipher: Cipher?) : RecoveryMethodIntent
    data object Pattern : RecoveryMethodIntent
    data object Email : RecoveryMethodIntent
    data object SecurityQuestion : RecoveryMethodIntent
    data object Skip : RecoveryMethodIntent
    data object Back : RecoveryMethodIntent
}

sealed interface RecoveryMethodEffect : UiEffect {
    data class VerifyFingerprint(val cipher: Cipher) : RecoveryMethodEffect
    data object FingerprintUnavailable : RecoveryMethodEffect
    data object OpenPattern : RecoveryMethodEffect
    data object OpenEmail : RecoveryMethodEffect
    data object OpenSecurityQuestion : RecoveryMethodEffect
    data object Finish : RecoveryMethodEffect
}

@KoinViewModel
class RecoveryMethodViewModel(
    @InjectedParam fromSettings: Boolean,
    observeOptions: ObserveRecoveryOptionsUseCase,
    private val fingerprint: FingerprintRecoveryUseCase,
    private val finishPrompt: FinishRecoveryPromptUseCase,
) : MviViewModel<RecoveryMethodState, RecoveryMethodIntent, RecoveryMethodEffect>(RecoveryMethodState(fromSettings)) {

    init {
        observeOptions().onEach { setState { copy(options = it) } }.launchIn(viewModelScope)
    }

    override fun handleIntent(intent: RecoveryMethodIntent) {
        when (intent) {
            is RecoveryMethodIntent.FingerprintToggled -> if (intent.enabled) {
                val cipher = fingerprint.enrollCipher()
                sendEffect(if (cipher != null) RecoveryMethodEffect.VerifyFingerprint(cipher) else RecoveryMethodEffect.FingerprintUnavailable)
            } else {
                viewModelScope.launch { fingerprint.disable() }
            }
            is RecoveryMethodIntent.FingerprintVerified -> viewModelScope.launch {
                val cipher = intent.cipher ?: return@launch sendEffect(RecoveryMethodEffect.FingerprintUnavailable)
                fingerprint.enable(cipher).onFailure { sendEffect(RecoveryMethodEffect.FingerprintUnavailable) }
            }
            RecoveryMethodIntent.Pattern -> sendEffect(RecoveryMethodEffect.OpenPattern)
            RecoveryMethodIntent.Email -> sendEffect(RecoveryMethodEffect.OpenEmail)
            RecoveryMethodIntent.SecurityQuestion -> sendEffect(RecoveryMethodEffect.OpenSecurityQuestion)
            RecoveryMethodIntent.Skip, RecoveryMethodIntent.Back -> viewModelScope.launch {
                finishPrompt()
                sendEffect(RecoveryMethodEffect.Finish)
            }
        }
    }
}
