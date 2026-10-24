package com.calculator.vault.feature.applock.overlay

import androidx.lifecycle.viewModelScope
import com.calculator.vault.core.domain.repository.AppLockRepository
import com.calculator.vault.core.domain.repository.SettingsRepository
import com.calculator.vault.core.domain.usecase.PinRules
import com.calculator.vault.core.model.AppLockAuthMethod
import com.calculator.vault.core.model.ThemeMode
import com.calculator.vault.core.ui.mvi.MviViewModel
import com.calculator.vault.core.ui.mvi.UiEffect
import com.calculator.vault.core.ui.mvi.UiIntent
import com.calculator.vault.core.ui.mvi.UiState
import com.calculator.vault.feature.applock.domain.usecase.VerifyAppLockCredentialUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam

data class LockOverlayState(
    val packageName: String,
    val ready: Boolean = false,
    val method: AppLockAuthMethod = AppLockAuthMethod.PIN,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val input: String = "",
    val error: Boolean = false,
    val errorTrigger: Int = 0,
    val patternResetKey: Int = 0,
) : UiState

sealed interface LockOverlayIntent : UiIntent {
    data class Digit(val digit: Int) : LockOverlayIntent
    data object Backspace : LockOverlayIntent
    data class Pattern(val pattern: List<Int>) : LockOverlayIntent
    data object FingerprintSucceeded : LockOverlayIntent
    data object UsePin : LockOverlayIntent
}

sealed interface LockOverlayEffect : UiEffect {
    data object PromptFingerprint : LockOverlayEffect
    data object Unlocked : LockOverlayEffect
}

@KoinViewModel
class LockOverlayViewModel(
    @InjectedParam packageName: String,
    private val appLock: AppLockRepository,
    private val settings: SettingsRepository,
    private val verify: VerifyAppLockCredentialUseCase,
) : MviViewModel<LockOverlayState, LockOverlayIntent, LockOverlayEffect>(LockOverlayState(packageName)) {

    init {
        viewModelScope.launch {
            val lock = appLock.settings.first()
            val method = if (lock.authMethod == AppLockAuthMethod.PATTERN && !lock.hasPattern) AppLockAuthMethod.PIN else lock.authMethod
            val themeMode = settings.settings.first().themeMode
            setState { copy(ready = true, method = method, themeMode = themeMode) }
            if (method == AppLockAuthMethod.FINGERPRINT) sendEffect(LockOverlayEffect.PromptFingerprint)
        }
    }

    override fun handleIntent(intent: LockOverlayIntent) {
        when (intent) {
            is LockOverlayIntent.Digit -> {
                if (currentState.input.length >= PinRules.LENGTH) return
                val input = currentState.input + intent.digit
                setState { copy(input = input, error = false) }
                if (input.length == PinRules.LENGTH) viewModelScope.launch {
                    if (verify.pin(input)) unlocked() else fail()
                }
            }
            LockOverlayIntent.Backspace -> setState { copy(input = input.dropLast(1)) }
            is LockOverlayIntent.Pattern -> viewModelScope.launch {
                if (verify.pattern(intent.pattern)) unlocked() else {
                    fail()
                    delay(700)
                    setState { copy(error = false, patternResetKey = patternResetKey + 1) }
                }
            }
            LockOverlayIntent.FingerprintSucceeded -> unlocked()
            LockOverlayIntent.UsePin -> setState { copy(method = AppLockAuthMethod.PIN) }
        }
    }

    private fun unlocked() {
        verify.markUnlocked(currentState.packageName)
        sendEffect(LockOverlayEffect.Unlocked)
    }

    private fun fail() = setState { copy(input = "", error = true, errorTrigger = errorTrigger + 1) }
}
