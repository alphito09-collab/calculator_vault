package com.calculator.vault.feature.intruder.presentation.mvi

import androidx.lifecycle.viewModelScope
import com.calculator.vault.core.common.VaultConstants
import com.calculator.vault.core.model.IntruderAttempt
import com.calculator.vault.core.model.IntruderStats
import com.calculator.vault.core.ui.mvi.MviViewModel
import com.calculator.vault.core.ui.mvi.UiEffect
import com.calculator.vault.core.ui.mvi.UiIntent
import com.calculator.vault.core.ui.mvi.UiState
import com.calculator.vault.feature.intruder.domain.usecase.IntruderSettingsUseCase
import com.calculator.vault.feature.intruder.domain.usecase.ObserveIntruderOverviewUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

data class IntruderState(
    val loading: Boolean = true,
    val detectionEnabled: Boolean = true,
    val selfieEnabled: Boolean = false,
    /** Wrong PINs in a row before an attempt is logged and photographed. */
    val attemptLimit: Int = VaultConstants.DEFAULT_INTRUDER_ATTEMPTS,
    val stats: IntruderStats = IntruderStats(),
    val attempts: List<IntruderAttempt> = emptyList(),
    val confirmClear: Boolean = false,
) : UiState

sealed interface IntruderIntent : UiIntent {
    data class Detection(val enabled: Boolean) : IntruderIntent
    data class Selfie(val enabled: Boolean) : IntruderIntent
    data class AttemptLimit(val count: Int) : IntruderIntent
    data class CameraPermissionResult(val granted: Boolean) : IntruderIntent
    data class OpenPhoto(val attempt: IntruderAttempt) : IntruderIntent
    data object Clear : IntruderIntent
    data object ConfirmClear : IntruderIntent
    data object DismissClear : IntruderIntent
    data object Back : IntruderIntent
}

sealed interface IntruderEffect : UiEffect {
    data object RequestCameraPermission : IntruderEffect
    data class OpenPhoto(val attemptId: String) : IntruderEffect
    data object Close : IntruderEffect
}

@KoinViewModel
class IntruderViewModel(
    observe: ObserveIntruderOverviewUseCase,
    private val settings: IntruderSettingsUseCase,
) : MviViewModel<IntruderState, IntruderIntent, IntruderEffect>(IntruderState()) {

    init {
        observe().onEach { o ->
            setState {
                copy(
                    loading = false,
                    detectionEnabled = o.detectionEnabled,
                    selfieEnabled = o.selfieEnabled,
                    attemptLimit = o.attemptLimit,
                    stats = o.stats,
                    attempts = o.attempts,
                )
            }
        }.launchIn(viewModelScope)
    }

    override fun handleIntent(intent: IntruderIntent) {
        when (intent) {
            is IntruderIntent.Detection -> viewModelScope.launch { settings.setDetection(intent.enabled) }
            is IntruderIntent.Selfie -> if (intent.enabled && !settings.hasCameraPermission()) {
                sendEffect(IntruderEffect.RequestCameraPermission)
            } else {
                viewModelScope.launch { settings.setSelfie(intent.enabled) }
            }
            is IntruderIntent.AttemptLimit -> viewModelScope.launch { settings.setAttemptLimit(intent.count) }
            is IntruderIntent.CameraPermissionResult -> if (intent.granted) viewModelScope.launch { settings.setSelfie(true) }
            is IntruderIntent.OpenPhoto -> if (intent.attempt.hasPhoto) sendEffect(IntruderEffect.OpenPhoto(intent.attempt.id))
            IntruderIntent.Clear -> if (currentState.attempts.isNotEmpty()) setState { copy(confirmClear = true) }
            IntruderIntent.DismissClear -> setState { copy(confirmClear = false) }
            IntruderIntent.ConfirmClear -> {
                setState { copy(confirmClear = false) }
                viewModelScope.launch { settings.clear() }
            }
            IntruderIntent.Back -> sendEffect(IntruderEffect.Close)
        }
    }
}
