package com.calculator.vault.feature.audios.presentation.mvi

import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.calculator.vault.core.domain.usecase.DecryptForSharingUseCase
import com.calculator.vault.core.domain.usecase.MoveToTrashUseCase
import com.calculator.vault.core.domain.usecase.ObserveVaultItemsUseCase
import com.calculator.vault.core.domain.usecase.RenameItemUseCase
import com.calculator.vault.core.domain.usecase.UnhideItemsUseCase
import com.calculator.vault.core.media.player.AudioController
import com.calculator.vault.core.media.player.AudioPlaybackState
import com.calculator.vault.core.model.MediaType
import com.calculator.vault.core.model.TransferProgress
import com.calculator.vault.core.model.VaultItem
import com.calculator.vault.core.ui.component.BulkAction
import com.calculator.vault.core.ui.mvi.MviViewModel
import com.calculator.vault.core.ui.mvi.UiEffect
import com.calculator.vault.core.ui.mvi.UiIntent
import com.calculator.vault.core.ui.mvi.UiState
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam
import java.io.File

data class AudioPlayerState(
    val playback: AudioPlaybackState = AudioPlaybackState(),
    val items: List<VaultItem> = emptyList(),
    val menuOpen: Boolean = false,
    val queueOpen: Boolean = false,
    val renaming: Boolean = false,
    val showDetails: Boolean = false,
    val confirm: BulkAction? = null,
    val progress: TransferProgress? = null,
) : UiState {
    val currentItem: VaultItem? get() = items.firstOrNull { it.id == playback.current?.id }
}

sealed interface AudioPlayerIntent : UiIntent {
    data object PlayPause : AudioPlayerIntent
    data object Next : AudioPlayerIntent
    data object Previous : AudioPlayerIntent
    data class Seek(val positionMs: Long) : AudioPlayerIntent
    data object Shuffle : AudioPlayerIntent
    data object Repeat : AudioPlayerIntent
    data object Speed : AudioPlayerIntent
    data object Equalizer : AudioPlayerIntent
    data object OpenQueue : AudioPlayerIntent
    data class PlayFromQueue(val index: Int) : AudioPlayerIntent
    data object OpenMenu : AudioPlayerIntent
    data object CloseMenu : AudioPlayerIntent
    data object Rename : AudioPlayerIntent
    data class RenameConfirmed(val name: String) : AudioPlayerIntent
    data object Details : AudioPlayerIntent
    data object Share : AudioPlayerIntent
    data object Delete : AudioPlayerIntent
    data object Unhide : AudioPlayerIntent
    data object Confirm : AudioPlayerIntent
    data object Dismiss : AudioPlayerIntent
    data object Back : AudioPlayerIntent
}

sealed interface AudioPlayerEffect : UiEffect {
    data object OpenEqualizer : AudioPlayerEffect
    data class ShareFiles(val files: List<File>, val mimeType: String) : AudioPlayerEffect
    data class Done(val action: BulkAction) : AudioPlayerEffect
    data object Failed : AudioPlayerEffect
    data object Close : AudioPlayerEffect
}

@UnstableApi
@KoinViewModel
class AudioPlayerViewModel(
    @InjectedParam startId: String,
    private val controller: AudioController,
    observeItems: ObserveVaultItemsUseCase,
    private val rename: RenameItemUseCase,
    private val moveToTrash: MoveToTrashUseCase,
    private val unhide: UnhideItemsUseCase,
    private val decrypt: DecryptForSharingUseCase,
) : MviViewModel<AudioPlayerState, AudioPlayerIntent, AudioPlayerEffect>(AudioPlayerState()) {

    init {
        combine(controller.state, observeItems(MediaType.AUDIO)) { playback, items -> playback to items }
            .onEach { (playback, items) -> setState { copy(playback = playback, items = items) } }
            .launchIn(viewModelScope)
        viewModelScope.launch {
            if (controller.state.value.current?.id != startId) {
                val items = observeItems(MediaType.AUDIO).first()
                controller.play(items.map { it.toTrack() }, items.indexOfFirst { it.id == startId }.coerceAtLeast(0))
            }
        }
    }

    override fun handleIntent(intent: AudioPlayerIntent) {
        when (intent) {
            AudioPlayerIntent.PlayPause -> controller.togglePlayPause()
            AudioPlayerIntent.Next -> controller.next()
            AudioPlayerIntent.Previous -> controller.previous()
            is AudioPlayerIntent.Seek -> controller.seekTo(intent.positionMs)
            AudioPlayerIntent.Shuffle -> controller.toggleShuffle()
            AudioPlayerIntent.Repeat -> controller.cycleRepeat()
            AudioPlayerIntent.Speed -> controller.cycleSpeed()
            AudioPlayerIntent.Equalizer -> sendEffect(AudioPlayerEffect.OpenEqualizer)
            AudioPlayerIntent.OpenQueue -> setState { copy(queueOpen = true) }
            is AudioPlayerIntent.PlayFromQueue -> {
                setState { copy(queueOpen = false) }
                controller.play(currentState.playback.queue, intent.index)
            }
            AudioPlayerIntent.OpenMenu -> setState { copy(menuOpen = true) }
            AudioPlayerIntent.CloseMenu -> setState { copy(menuOpen = false) }
            AudioPlayerIntent.Rename -> setState { copy(menuOpen = false, renaming = true) }
            is AudioPlayerIntent.RenameConfirmed -> viewModelScope.launch {
                val item = currentState.currentItem ?: return@launch
                setState { copy(renaming = false) }
                rename(item, intent.name).onFailure { sendEffect(AudioPlayerEffect.Failed) }
            }
            AudioPlayerIntent.Details -> setState { copy(menuOpen = false, showDetails = true) }
            AudioPlayerIntent.Share -> {
                setState { copy(menuOpen = false) }
                val item = currentState.currentItem ?: return
                viewModelScope.launch {
                    decrypt(listOf(item.id))
                        .onSuccess { sendEffect(AudioPlayerEffect.ShareFiles(it, item.mimeType)) }
                        .onFailure { sendEffect(AudioPlayerEffect.Failed) }
                }
            }
            AudioPlayerIntent.Delete -> setState { copy(menuOpen = false, confirm = BulkAction.Delete) }
            AudioPlayerIntent.Unhide -> setState { copy(menuOpen = false, confirm = BulkAction.Unhide) }
            AudioPlayerIntent.Dismiss -> setState { copy(confirm = null, renaming = false, showDetails = false, queueOpen = false) }
            AudioPlayerIntent.Confirm -> confirm()
            AudioPlayerIntent.Back -> sendEffect(AudioPlayerEffect.Close)
        }
    }

    private fun confirm() {
        val action = currentState.confirm ?: return
        val item = currentState.currentItem ?: return
        setState { copy(confirm = null) }
        viewModelScope.launch {
            controller.stop()
            when (action) {
                BulkAction.Delete -> moveToTrash(listOf(item.id))
                    .onSuccess { sendEffect(AudioPlayerEffect.Done(action)) }
                    .onFailure { sendEffect(AudioPlayerEffect.Failed) }
                BulkAction.Unhide -> {
                    setState { copy(progress = TransferProgress(0, 1)) }
                    unhide(listOf(item.id)).onEach { setState { copy(progress = it) } }.collect()
                    setState { copy(progress = null) }
                    sendEffect(AudioPlayerEffect.Done(action))
                }
                else -> Unit
            }
        }
    }
}
