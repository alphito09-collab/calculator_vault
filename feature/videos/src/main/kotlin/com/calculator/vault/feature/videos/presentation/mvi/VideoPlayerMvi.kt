package com.calculator.vault.feature.videos.presentation.mvi

import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.calculator.vault.core.domain.usecase.DecryptForSharingUseCase
import com.calculator.vault.core.domain.usecase.MoveToTrashUseCase
import com.calculator.vault.core.domain.usecase.ObserveVaultItemsUseCase
import com.calculator.vault.core.model.MediaType
import com.calculator.vault.core.model.VaultItem
import com.calculator.vault.core.media.player.EncryptedDataSource
import com.calculator.vault.core.media.player.VaultPlayerFactory
import com.calculator.vault.core.ui.mvi.MviViewModel
import com.calculator.vault.core.ui.mvi.UiEffect
import com.calculator.vault.core.ui.mvi.UiIntent
import com.calculator.vault.core.ui.mvi.UiState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam
import java.io.File

data class VideoPlayerState(
    val items: List<VaultItem> = emptyList(),
    val currentIndex: Int = 0,
    val chromeVisible: Boolean = true,
    val confirmDelete: Boolean = false,
) : UiState {
    val current: VaultItem? get() = items.getOrNull(currentIndex)
}

sealed interface VideoPlayerIntent : UiIntent {
    data class ControllerVisibility(val visible: Boolean) : VideoPlayerIntent
    data object Share : VideoPlayerIntent
    data object Delete : VideoPlayerIntent
    data object ConfirmDelete : VideoPlayerIntent
    data object DismissDelete : VideoPlayerIntent
    data object Pause : VideoPlayerIntent
    data object Back : VideoPlayerIntent
}

sealed interface VideoPlayerEffect : UiEffect {
    data class ShareFiles(val files: List<File>, val mimeType: String) : VideoPlayerEffect
    data object Deleted : VideoPlayerEffect
    data object Failed : VideoPlayerEffect
    data object Close : VideoPlayerEffect
}

/** Owns the ExoPlayer for the screen; released with the ViewModel (and therefore on lock). */
@UnstableApi
@KoinViewModel
class VideoPlayerViewModel(
    @InjectedParam private val startId: String,
    factory: VaultPlayerFactory,
    private val observeItems: ObserveVaultItemsUseCase,
    private val moveToTrash: MoveToTrashUseCase,
    private val decrypt: DecryptForSharingUseCase,
) : MviViewModel<VideoPlayerState, VideoPlayerIntent, VideoPlayerEffect>(VideoPlayerState()) {

    val player: ExoPlayer = factory.create()

    private val listener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            setState { copy(currentIndex = player.currentMediaItemIndex) }
        }
    }

    init {
        player.addListener(listener)
        viewModelScope.launch {
            val items = observeItems(MediaType.VIDEO).first()
            val start = items.indexOfFirst { it.id == startId }.coerceAtLeast(0)
            setState { copy(items = items, currentIndex = start) }
            player.setMediaItems(
                items.map { item ->
                    MediaItem.Builder()
                        .setMediaId(item.id)
                        .setUri(EncryptedDataSource.uriFor(item.id))
                        .setMediaMetadata(MediaMetadata.Builder().setTitle(item.name).build())
                        .build()
                },
                start,
                0L,
            )
            player.prepare()
            player.play()
        }
    }

    override fun handleIntent(intent: VideoPlayerIntent) {
        when (intent) {
            is VideoPlayerIntent.ControllerVisibility -> setState { copy(chromeVisible = intent.visible) }
            VideoPlayerIntent.Share -> {
                val item = currentState.current ?: return
                player.pause()
                viewModelScope.launch {
                    decrypt(listOf(item.id))
                        .onSuccess { sendEffect(VideoPlayerEffect.ShareFiles(it, item.mimeType)) }
                        .onFailure { sendEffect(VideoPlayerEffect.Failed) }
                }
            }
            VideoPlayerIntent.Delete -> {
                player.pause()
                setState { copy(confirmDelete = true) }
            }
            VideoPlayerIntent.DismissDelete -> setState { copy(confirmDelete = false) }
            VideoPlayerIntent.ConfirmDelete -> {
                val item = currentState.current ?: return
                setState { copy(confirmDelete = false) }
                viewModelScope.launch {
                    moveToTrash(listOf(item.id))
                        .onSuccess { sendEffect(VideoPlayerEffect.Deleted) }
                        .onFailure { sendEffect(VideoPlayerEffect.Failed) }
                }
            }
            VideoPlayerIntent.Pause -> player.pause()
            VideoPlayerIntent.Back -> sendEffect(VideoPlayerEffect.Close)
        }
    }

    override fun onCleared() {
        player.removeListener(listener)
        player.release()
    }
}
