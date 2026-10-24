package com.calculator.vault.feature.camera.presentation.mvi

import androidx.lifecycle.viewModelScope
import com.calculator.vault.core.domain.usecase.AddCaptureUseCase
import com.calculator.vault.core.model.MediaType
import com.calculator.vault.core.ui.mvi.MviViewModel
import com.calculator.vault.core.ui.mvi.UiEffect
import com.calculator.vault.core.ui.mvi.UiIntent
import com.calculator.vault.core.ui.mvi.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam
import java.io.File

enum class CaptureMode { PHOTO, VIDEO }

data class CameraState(
    val mode: CaptureMode = CaptureMode.PHOTO,
    val frontLens: Boolean = false,
    val flashOn: Boolean = false,
    val recording: Boolean = false,
    val recordingSeconds: Int = 0,
    val saving: Int = 0,
    val savedCount: Int = 0,
    val lastSavedId: String? = null,
) : UiState

sealed interface CameraIntent : UiIntent {
    data class SetMode(val mode: CaptureMode) : CameraIntent
    data object SwitchLens : CameraIntent
    data object ToggleFlash : CameraIntent
    data object RecordingStarted : CameraIntent
    data object RecordingStopped : CameraIntent
    data class Captured(val file: File, val type: MediaType) : CameraIntent
    data object CaptureFailed : CameraIntent
    data object Close : CameraIntent
}

sealed interface CameraEffect : UiEffect {
    data object Failed : CameraEffect
    data class Close(val savedCount: Int) : CameraEffect
}

/** Keeps camera UI state and encrypts every capture into the vault as soon as it lands. */
@KoinViewModel
class CameraViewModel(
    @InjectedParam startWithVideo: Boolean,
    private val addCapture: AddCaptureUseCase,
) : MviViewModel<CameraState, CameraIntent, CameraEffect>(CameraState(mode = if (startWithVideo) CaptureMode.VIDEO else CaptureMode.PHOTO)) {

    private var timer: Job? = null

    override fun handleIntent(intent: CameraIntent) {
        when (intent) {
            is CameraIntent.SetMode -> if (!currentState.recording) setState { copy(mode = intent.mode) }
            CameraIntent.SwitchLens -> if (!currentState.recording) setState { copy(frontLens = !frontLens) }
            CameraIntent.ToggleFlash -> setState { copy(flashOn = !flashOn) }
            CameraIntent.RecordingStarted -> {
                setState { copy(recording = true, recordingSeconds = 0) }
                timer = viewModelScope.launch {
                    while (isActive) {
                        delay(1_000)
                        setState { copy(recordingSeconds = recordingSeconds + 1) }
                    }
                }
            }
            CameraIntent.RecordingStopped -> {
                timer?.cancel()
                setState { copy(recording = false) }
            }
            is CameraIntent.Captured -> save(intent.file, intent.type)
            CameraIntent.CaptureFailed -> sendEffect(CameraEffect.Failed)
            CameraIntent.Close -> sendEffect(CameraEffect.Close(currentState.savedCount))
        }
    }

    private fun save(file: File, type: MediaType) {
        setState { copy(saving = saving + 1) }
        viewModelScope.launch {
            val mime = if (type == MediaType.VIDEO) "video/mp4" else "image/jpeg"
            addCapture(file, type, mime)
                .onSuccess { item -> setState { copy(saving = saving - 1, savedCount = savedCount + 1, lastSavedId = item.id) } }
                .onFailure {
                    setState { copy(saving = saving - 1) }
                    sendEffect(CameraEffect.Failed)
                }
        }
    }

    override fun onCleared() {
        timer?.cancel()
    }
}
