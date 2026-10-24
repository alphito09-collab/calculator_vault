package com.calculator.vault.feature.audios.presentation.ui

import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import com.calculator.vault.core.designsystem.icon.VaultIcons
import com.calculator.vault.core.designsystem.preview.ScreenPreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.media.player.AudioPlaybackState
import com.calculator.vault.core.model.MediaType
import com.calculator.vault.core.model.VaultItem
import com.calculator.vault.core.ui.component.VaultItemRow
import com.calculator.vault.core.ui.gallery.GalleryEffects
import com.calculator.vault.core.ui.gallery.GalleryIntent
import com.calculator.vault.core.ui.gallery.GalleryScaffold
import com.calculator.vault.core.ui.gallery.GalleryState
import com.calculator.vault.core.ui.util.formatBytes
import com.calculator.vault.core.ui.util.albumLabel
import com.calculator.vault.core.ui.util.formatDuration
import com.calculator.vault.feature.audios.R
import com.calculator.vault.feature.audios.presentation.components.MiniPlayer
import com.calculator.vault.feature.audios.presentation.mvi.AudiosViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(UnstableApi::class)
@Composable
fun AudiosRoute(onOpenPlayer: (String) -> Unit, onImport: () -> Unit, onBack: () -> Unit, viewModel: AudiosViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val playback by viewModel.playback.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    GalleryEffects(viewModel.effects, snackbar, onOpen = viewModel::play, onImport = onImport, onBack = onBack)
    AudiosScreen(
        state = state,
        playback = playback,
        onIntent = viewModel::onIntent,
        onPlayPause = viewModel::togglePlayPause,
        onOpenPlayer = { playback.current?.let { onOpenPlayer(it.id) } },
        snackbar = snackbar,
    )
}

@Composable
fun AudiosScreen(
    state: GalleryState,
    playback: AudioPlaybackState,
    onIntent: (GalleryIntent) -> Unit,
    onPlayPause: () -> Unit,
    onOpenPlayer: () -> Unit,
    snackbar: SnackbarHostState = remember { SnackbarHostState() },
) {
    Box(Modifier.fillMaxSize()) {
        GalleryScaffold(
            state = state,
            onIntent = onIntent,
            emptyIcon = VaultIcons.Music,
            emptyTitle = stringResource(R.string.audios_empty_title),
            emptyMessage = stringResource(R.string.audios_empty_message),
            importLabel = stringResource(R.string.audios_import),
            snackbar = snackbar,
            showLayoutOptions = false,
        ) {
            LazyColumn(
                Modifier.align(Alignment.TopCenter).widthIn(max = 720.dp),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 200.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(state.items, key = { it.id }) { item ->
                    VaultItemRow(
                        item = item,
                        subtitle = subtitleFor(item),
                        onClick = { onIntent(GalleryIntent.Open(item)) },
                        onLongClick = { onIntent(GalleryIntent.Toggle(item)) },
                        selectionMode = state.selecting,
                        selected = item.id in state.selected,
                        playing = playback.current?.id == item.id,
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
        val current = playback.current
        AnimatedVisibility(
            visible = current != null && !state.selecting,
            enter = slideInVertically { it },
            exit = slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(start = 20.dp, end = 20.dp, bottom = 100.dp).widthIn(max = 520.dp),
        ) {
            if (current != null) {
                MiniPlayer(current.title, "${current.subtitle} • ${formatDuration(playback.durationMs)}", current.id, playback.isPlaying, onOpenPlayer, onPlayPause)
            }
        }
    }
}

@Composable
private fun subtitleFor(item: VaultItem): String =
    listOfNotNull(item.album?.let { albumLabel(it) } ?: stringResource(R.string.audios_recorded), item.durationMs?.let(::formatDuration) ?: formatBytes(item.sizeBytes)).joinToString(" • ")

@ScreenPreviews
@Composable
private fun AudiosPreview() = VaultPreview {
    val items = listOf("Voice memo", "Late Night Notes", "Piano Idea", "Meeting Backup").mapIndexed { i, n ->
        VaultItem("$i", MediaType.AUDIO, "$n.m4a", "audio/mp4", 1, null, "Recorded", 0, 0, durationMs = 238_000)
    }
    AudiosScreen(
        GalleryState(MediaType.AUDIO, loading = false, items = items),
        AudioPlaybackState(queue = listOf(com.calculator.vault.core.media.player.AudioTrack("3", "Meeting Backup", "Recorded")), currentIndex = 0, isPlaying = true, durationMs = 238_000),
        onIntent = {},
        onPlayPause = {},
        onOpenPlayer = {},
    )
}
