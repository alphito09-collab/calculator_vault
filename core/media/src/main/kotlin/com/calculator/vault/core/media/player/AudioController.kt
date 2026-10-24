package com.calculator.vault.core.media.player

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single

data class AudioTrack(val id: String, val title: String, val subtitle: String)

enum class RepeatMode { OFF, ALL, ONE }

data class AudioPlaybackState(
    val queue: List<AudioTrack> = emptyList(),
    val currentIndex: Int = -1,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val shuffle: Boolean = false,
    val repeat: RepeatMode = RepeatMode.OFF,
    val speed: Float = 1f,
) {
    val current: AudioTrack? get() = queue.getOrNull(currentIndex)
    val isActive: Boolean get() = current != null
}

/**
 * One shared audio player so playback continues between the list and the player screen and
 * the mini-player can show what's playing. All ExoPlayer calls happen on the main thread.
 */
@Single
@UnstableApi
class AudioController(private val factory: VaultPlayerFactory) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var player: ExoPlayer? = null
    private var ticker: Job? = null

    val effects = AudioEffects()

    private val _state = MutableStateFlow(AudioPlaybackState())
    val state: StateFlow<AudioPlaybackState> = _state.asStateFlow()

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _state.update { it.copy(isPlaying = isPlaying) }
            if (isPlaying) startTicker() else ticker?.cancel()
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) = syncFromPlayer()

        override fun onPlaybackStateChanged(playbackState: Int) = syncFromPlayer()

        override fun onAudioSessionIdChanged(audioSessionId: Int) {
            if (audioSessionId != 0) effects.attach(audioSessionId)
        }
    }

    fun play(queue: List<AudioTrack>, startIndex: Int) {
        val p = player ?: factory.create().also {
            it.addListener(listener)
            player = it
            effects.attach(it.audioSessionId)
        }
        p.setMediaItems(
            queue.map { track ->
                MediaItem.Builder()
                    .setMediaId(track.id)
                    .setUri(EncryptedDataSource.uriFor(track.id))
                    .setMediaMetadata(MediaMetadata.Builder().setTitle(track.title).setArtist(track.subtitle).build())
                    .build()
            },
            startIndex.coerceIn(0, (queue.size - 1).coerceAtLeast(0)),
            0L,
        )
        p.prepare()
        p.play()
        _state.update { it.copy(queue = queue, currentIndex = startIndex) }
    }

    fun togglePlayPause() {
        val p = player ?: return
        if (p.isPlaying) p.pause() else p.play()
    }

    fun next() = player?.seekToNextMediaItem() ?: Unit
    fun previous() = player?.seekToPrevious() ?: Unit

    fun seekTo(positionMs: Long) {
        player?.seekTo(positionMs)
        _state.update { it.copy(positionMs = positionMs) }
    }

    fun toggleShuffle() {
        val p = player ?: return
        p.shuffleModeEnabled = !p.shuffleModeEnabled
        _state.update { it.copy(shuffle = p.shuffleModeEnabled) }
    }

    fun cycleRepeat() {
        val p = player ?: return
        val next = when (_state.value.repeat) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        p.repeatMode = when (next) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
        }
        _state.update { it.copy(repeat = next) }
    }

    fun cycleSpeed() {
        val speeds = listOf(1f, 1.25f, 1.5f, 2f, 0.75f)
        val next = speeds[(speeds.indexOf(_state.value.speed) + 1) % speeds.size]
        player?.playbackParameters = PlaybackParameters(next)
        _state.update { it.copy(speed = next) }
    }

    /** Called on lock and when the user closes the mini-player. */
    fun stop() {
        ticker?.cancel()
        player?.let {
            it.removeListener(listener)
            it.release()
        }
        player = null
        effects.release()
        _state.value = AudioPlaybackState()
    }

    private fun syncFromPlayer() {
        val p = player ?: return
        _state.update {
            it.copy(
                currentIndex = p.currentMediaItemIndex,
                durationMs = p.duration.coerceAtLeast(0),
                positionMs = p.currentPosition.coerceAtLeast(0),
            )
        }
    }

    private fun startTicker() {
        ticker?.cancel()
        ticker = scope.launch {
            while (isActive) {
                syncFromPlayer()
                delay(250)
            }
        }
    }
}
