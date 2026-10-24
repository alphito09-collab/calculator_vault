package com.calculator.vault.feature.audios.presentation.mvi

import androidx.media3.common.util.UnstableApi
import com.calculator.vault.core.domain.repository.SettingsRepository
import com.calculator.vault.core.domain.usecase.DecryptForSharingUseCase
import com.calculator.vault.core.domain.usecase.GroupByDateUseCase
import com.calculator.vault.core.domain.usecase.MoveToTrashUseCase
import com.calculator.vault.core.domain.usecase.ObserveVaultItemsUseCase
import com.calculator.vault.core.domain.usecase.UnhideItemsUseCase
import com.calculator.vault.core.media.player.AudioController
import com.calculator.vault.core.media.player.AudioPlaybackState
import com.calculator.vault.core.media.player.AudioTrack
import com.calculator.vault.core.model.MediaType
import com.calculator.vault.core.model.VaultItem
import com.calculator.vault.core.ui.gallery.GalleryViewModel
import kotlinx.coroutines.flow.StateFlow
import org.koin.android.annotation.KoinViewModel

/** Audio list: the shared gallery logic plus the playback state for the mini player. */
@UnstableApi
@KoinViewModel
class AudiosViewModel(
    observeItems: ObserveVaultItemsUseCase,
    groupByDate: GroupByDateUseCase,
    settings: SettingsRepository,
    moveToTrash: MoveToTrashUseCase,
    unhideItems: UnhideItemsUseCase,
    decryptForSharing: DecryptForSharingUseCase,
    private val controller: AudioController,
) : GalleryViewModel(MediaType.AUDIO, observeItems, groupByDate, settings, moveToTrash, unhideItems, decryptForSharing) {

    val playback: StateFlow<AudioPlaybackState> = controller.state

    fun play(item: VaultItem) {
        val items = state.value.items
        if (playback.value.current?.id == item.id) {
            controller.togglePlayPause()
            return
        }
        controller.play(items.map { it.toTrack() }, items.indexOfFirst { it.id == item.id }.coerceAtLeast(0))
    }

    fun togglePlayPause() = controller.togglePlayPause()

    fun stop() = controller.stop()
}

internal fun VaultItem.toTrack() = AudioTrack(id, name.substringBeforeLast('.'), album ?: "Recorded")
