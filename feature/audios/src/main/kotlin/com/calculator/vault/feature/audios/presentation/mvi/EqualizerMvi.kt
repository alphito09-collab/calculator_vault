package com.calculator.vault.feature.audios.presentation.mvi

import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.calculator.vault.core.media.player.AudioController
import com.calculator.vault.core.media.player.EqualizerState
import com.calculator.vault.core.media.player.ReverbPreset
import com.calculator.vault.core.ui.mvi.MviViewModel
import com.calculator.vault.core.ui.mvi.UiEffect
import com.calculator.vault.core.ui.mvi.UiIntent
import com.calculator.vault.core.ui.mvi.UiState
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.annotation.KoinViewModel

data class EqualizerScreenState(val eq: EqualizerState = EqualizerState(), val reverbMenuOpen: Boolean = false) : UiState

sealed interface EqualizerIntent : UiIntent {
    data class Enabled(val enabled: Boolean) : EqualizerIntent
    data class Preset(val index: Int) : EqualizerIntent
    data class Band(val band: Int, val levelDb: Float) : EqualizerIntent
    data class Bass(val value: Float) : EqualizerIntent
    data class Virtualizer(val value: Float) : EqualizerIntent
    data object OpenReverb : EqualizerIntent
    data object CloseReverb : EqualizerIntent
    data class Reverb(val preset: ReverbPreset) : EqualizerIntent
    data object Back : EqualizerIntent
}

sealed interface EqualizerEffect : UiEffect {
    data object Close : EqualizerEffect
}

@UnstableApi
@KoinViewModel
class EqualizerViewModel(private val controller: AudioController) :
    MviViewModel<EqualizerScreenState, EqualizerIntent, EqualizerEffect>(EqualizerScreenState()) {

    private val audioFx get() = controller.effects

    init {
        audioFx.state.onEach { setState { copy(eq = it) } }.launchIn(viewModelScope)
    }

    override fun handleIntent(intent: EqualizerIntent) {
        when (intent) {
            is EqualizerIntent.Enabled -> audioFx.setEnabled(intent.enabled)
            is EqualizerIntent.Preset -> { audioFx.setEnabled(true); audioFx.usePreset(intent.index) }
            is EqualizerIntent.Band -> audioFx.setBandLevel(intent.band, intent.levelDb)
            is EqualizerIntent.Bass -> audioFx.setBassBoost(intent.value)
            is EqualizerIntent.Virtualizer -> audioFx.setVirtualizer(intent.value)
            EqualizerIntent.OpenReverb -> setState { copy(reverbMenuOpen = true) }
            EqualizerIntent.CloseReverb -> setState { copy(reverbMenuOpen = false) }
            is EqualizerIntent.Reverb -> {
                setState { copy(reverbMenuOpen = false) }
                audioFx.setReverb(intent.preset)
            }
            EqualizerIntent.Back -> sendEffect(EqualizerEffect.Close)
        }
    }
}
