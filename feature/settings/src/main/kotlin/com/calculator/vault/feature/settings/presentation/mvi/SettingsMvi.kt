package com.calculator.vault.feature.settings.presentation.mvi

import androidx.lifecycle.viewModelScope
import com.calculator.vault.core.ui.mvi.MviViewModel
import com.calculator.vault.core.ui.mvi.UiEffect
import com.calculator.vault.core.ui.mvi.UiIntent
import com.calculator.vault.core.ui.mvi.UiState
import com.calculator.vault.feature.settings.domain.repository.UninstallProtection
import com.calculator.vault.feature.settings.domain.usecase.FingerprintUnlockUseCase
import com.calculator.vault.feature.settings.domain.usecase.ObserveSettingsOverviewUseCase
import com.calculator.vault.feature.settings.domain.usecase.SettingsOverview
import com.calculator.vault.feature.settings.domain.usecase.UpdateSettingsUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import javax.crypto.Cipher

/** Screens reachable from Settings; the app module maps them to routes. */
enum class SettingsDestination {
    ACCOUNT, THEME, APP_ICON, LANGUAGE, APP_LOCK, CHANGE_PIN, APP_LOCK_SECURITY, RECOVERY, INTRUDER, CLOUD_BACKUP, TRASH,
}

enum class ExternalAction { FEEDBACK, RATE, SHARE, PRIVACY, TERMS }

data class SettingsState(
    val overview: SettingsOverview? = null,
    val uninstallProtected: Boolean = false,
    val showUninstallSheet: Boolean = false,
) : UiState

sealed interface SettingsIntent : UiIntent {
    data class Open(val destination: SettingsDestination) : SettingsIntent
    data class External(val action: ExternalAction) : SettingsIntent
    data class Fingerprint(val enabled: Boolean) : SettingsIntent
    data class FingerprintVerified(val cipher: Cipher?) : SettingsIntent
    data class Panic(val enabled: Boolean) : SettingsIntent
    data class IntruderSelfie(val enabled: Boolean) : SettingsIntent
    data class CameraPermission(val granted: Boolean) : SettingsIntent
    data class PreventScreenshots(val enabled: Boolean) : SettingsIntent
    data class PreventUninstall(val enabled: Boolean) : SettingsIntent
    data object ActivateUninstallProtection : SettingsIntent
    data object DismissUninstallSheet : SettingsIntent
    data class Notifications(val enabled: Boolean) : SettingsIntent
    data object DismissCloudBanner : SettingsIntent
    data object Resumed : SettingsIntent
}

sealed interface SettingsEffect : UiEffect {
    data class Navigate(val destination: SettingsDestination) : SettingsEffect
    data class RunExternal(val action: ExternalAction) : SettingsEffect
    data class PromptFingerprint(val cipher: Cipher) : SettingsEffect
    data object FingerprintUnavailable : SettingsEffect
    data object RequestCamera : SettingsEffect
    data object RequestNotifications : SettingsEffect
    data object LaunchDeviceAdmin : SettingsEffect
    data object UninstallProtectionOff : SettingsEffect
}

@KoinViewModel
class SettingsViewModel(
    observe: ObserveSettingsOverviewUseCase,
    private val update: UpdateSettingsUseCase,
    private val fingerprint: FingerprintUnlockUseCase,
    private val uninstall: UninstallProtection,
) : MviViewModel<SettingsState, SettingsIntent, SettingsEffect>(SettingsState()) {

    init {
        observe().onEach { setState { copy(overview = it) } }.launchIn(viewModelScope)
        setState { copy(uninstallProtected = uninstall.isActive()) }
    }

    override fun handleIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.Open -> sendEffect(SettingsEffect.Navigate(intent.destination))
            is SettingsIntent.External -> sendEffect(SettingsEffect.RunExternal(intent.action))
            is SettingsIntent.Fingerprint -> if (intent.enabled) {
                val cipher = fingerprint.enrollCipher()
                sendEffect(if (cipher != null) SettingsEffect.PromptFingerprint(cipher) else SettingsEffect.FingerprintUnavailable)
            } else {
                viewModelScope.launch { fingerprint.disable() }
            }
            is SettingsIntent.FingerprintVerified -> viewModelScope.launch {
                val cipher = intent.cipher ?: return@launch sendEffect(SettingsEffect.FingerprintUnavailable)
                fingerprint.enable(cipher).onFailure { sendEffect(SettingsEffect.FingerprintUnavailable) }
            }
            is SettingsIntent.Panic -> viewModelScope.launch { update.panic(intent.enabled) }
            is SettingsIntent.IntruderSelfie -> if (intent.enabled && !update.hasCameraPermission()) {
                sendEffect(SettingsEffect.RequestCamera)
            } else {
                viewModelScope.launch { update.intruderSelfie(intent.enabled) }
            }
            is SettingsIntent.CameraPermission -> if (intent.granted) viewModelScope.launch { update.intruderSelfie(true) }
            is SettingsIntent.PreventScreenshots -> viewModelScope.launch { update.preventScreenshots(intent.enabled) }
            is SettingsIntent.PreventUninstall -> if (intent.enabled) {
                setState { copy(showUninstallSheet = true) }
            } else {
                uninstall.deactivate()
                setState { copy(uninstallProtected = false) }
                sendEffect(SettingsEffect.UninstallProtectionOff)
            }
            SettingsIntent.ActivateUninstallProtection -> {
                setState { copy(showUninstallSheet = false) }
                sendEffect(SettingsEffect.LaunchDeviceAdmin)
            }
            SettingsIntent.DismissUninstallSheet -> setState { copy(showUninstallSheet = false) }
            is SettingsIntent.Notifications -> viewModelScope.launch {
                update.notifications(intent.enabled)
                if (intent.enabled) sendEffect(SettingsEffect.RequestNotifications)
            }
            SettingsIntent.DismissCloudBanner -> viewModelScope.launch { update.dismissCloudBanner() }
            SettingsIntent.Resumed -> setState { copy(uninstallProtected = uninstall.isActive()) }
        }
    }
}
