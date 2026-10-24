package com.calculator.vault.core.media.player

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.PresetReverb
import android.media.audiofx.Virtualizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

data class EqBand(val centerHz: Int, val levelDb: Float, val minDb: Float, val maxDb: Float)

enum class ReverbPreset(val id: Short) { NONE(0), SMALL_ROOM(1), MEDIUM_ROOM(2), LARGE_ROOM(3), MEDIUM_HALL(4), LARGE_HALL(5), PLATE(6) }

data class EqualizerState(
    val available: Boolean = false,
    val enabled: Boolean = false,
    val presets: List<String> = emptyList(),
    val currentPreset: Int = -1,
    val bands: List<EqBand> = emptyList(),
    val bassBoost: Float = 0f,
    val virtualizer: Float = 0f,
    val reverb: ReverbPreset = ReverbPreset.NONE,
)

/**
 * Wraps the platform audio effects for one audio session. Some devices/emulators don't ship
 * every effect, so each is optional and failures only disable that control.
 */
class AudioEffects {
    private var equalizer: Equalizer? = null
    private var bass: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var reverb: PresetReverb? = null

    private val _state = MutableStateFlow(EqualizerState())
    val state: StateFlow<EqualizerState> = _state.asStateFlow()

    fun attach(audioSessionId: Int) {
        release()
        val previous = _state.value
        equalizer = runCatching { Equalizer(0, audioSessionId) }.onFailure { Timber.w(it, "No equalizer") }.getOrNull()
        bass = runCatching { BassBoost(0, audioSessionId) }.getOrNull()
        virtualizer = runCatching { Virtualizer(0, audioSessionId) }.getOrNull()
        reverb = runCatching { PresetReverb(0, audioSessionId) }.getOrNull()
        val eq = equalizer
        if (eq == null) {
            _state.value = EqualizerState(available = false)
            return
        }
        val (min, max) = eq.bandLevelRange.let { it[0] / 100f to it[1] / 100f }
        _state.value = EqualizerState(
            available = true,
            enabled = previous.enabled,
            presets = (0 until eq.numberOfPresets).map { eq.getPresetName(it.toShort()) },
            currentPreset = previous.currentPreset,
            bands = (0 until eq.numberOfBands).map { band ->
                EqBand(
                    centerHz = eq.getCenterFreq(band.toShort()) / 1000,
                    levelDb = previous.bands.getOrNull(band)?.levelDb ?: (eq.getBandLevel(band.toShort()) / 100f),
                    minDb = min,
                    maxDb = max,
                )
            },
            bassBoost = previous.bassBoost,
            virtualizer = previous.virtualizer,
            reverb = previous.reverb,
        )
        applyAll()
    }

    fun setEnabled(enabled: Boolean) {
        _state.update { it.copy(enabled = enabled) }
        applyAll()
    }

    fun usePreset(index: Int) {
        val eq = equalizer ?: return
        runCatching {
            eq.usePreset(index.toShort())
            _state.update { s ->
                s.copy(
                    currentPreset = index,
                    bands = s.bands.mapIndexed { band, b -> b.copy(levelDb = eq.getBandLevel(band.toShort()) / 100f) },
                )
            }
        }
    }

    fun setBandLevel(band: Int, levelDb: Float) {
        _state.update { s ->
            s.copy(currentPreset = -1, bands = s.bands.mapIndexed { i, b -> if (i == band) b.copy(levelDb = levelDb) else b })
        }
        runCatching { equalizer?.setBandLevel(band.toShort(), (levelDb * 100).toInt().toShort()) }
    }

    fun setBassBoost(strength: Float) {
        _state.update { it.copy(bassBoost = strength) }
        runCatching { bass?.setStrength((strength * 1000).toInt().toShort()) }
    }

    fun setVirtualizer(strength: Float) {
        _state.update { it.copy(virtualizer = strength) }
        runCatching { virtualizer?.setStrength((strength * 1000).toInt().toShort()) }
    }

    fun setReverb(preset: ReverbPreset) {
        _state.update { it.copy(reverb = preset) }
        runCatching { reverb?.preset = preset.id }
    }

    private fun applyAll() {
        val s = _state.value
        runCatching {
            equalizer?.enabled = s.enabled
            bass?.enabled = s.enabled && s.bassBoost > 0f
            virtualizer?.enabled = s.enabled && s.virtualizer > 0f
            reverb?.enabled = s.enabled && s.reverb != ReverbPreset.NONE
            if (s.enabled) {
                if (s.currentPreset >= 0) equalizer?.usePreset(s.currentPreset.toShort())
                else s.bands.forEachIndexed { i, b -> equalizer?.setBandLevel(i.toShort(), (b.levelDb * 100).toInt().toShort()) }
                bass?.setStrength((s.bassBoost * 1000).toInt().toShort())
                virtualizer?.setStrength((s.virtualizer * 1000).toInt().toShort())
                reverb?.preset = s.reverb.id
            }
        }.onFailure { Timber.w(it, "Applying audio effects failed") }
    }

    fun release() {
        listOf(equalizer, bass, virtualizer, reverb).forEach { runCatching { it?.release() } }
        equalizer = null; bass = null; virtualizer = null; reverb = null
    }
}
