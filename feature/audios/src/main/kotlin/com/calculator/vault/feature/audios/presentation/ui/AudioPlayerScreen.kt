package com.calculator.vault.feature.audios.presentation.ui

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import com.calculator.vault.core.designsystem.component.RowTrailing
import com.calculator.vault.core.designsystem.component.SettingsRow
import com.calculator.vault.core.designsystem.component.ToastKind
import com.calculator.vault.core.designsystem.component.VaultBottomSheet
import com.calculator.vault.core.designsystem.component.VaultDropdownMenu
import com.calculator.vault.core.designsystem.component.VaultIconButton
import com.calculator.vault.core.designsystem.component.VaultMenuItem
import com.calculator.vault.core.designsystem.component.VaultSlider
import com.calculator.vault.core.designsystem.component.VaultSnackbarHost
import com.calculator.vault.core.designsystem.component.VaultTopBar
import com.calculator.vault.core.designsystem.component.isLandscape
import com.calculator.vault.core.designsystem.component.showToast
import com.calculator.vault.core.designsystem.icon.VaultIcons
import com.calculator.vault.core.designsystem.preview.ScreenPreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultTheme
import com.calculator.vault.core.media.player.AudioPlaybackState
import com.calculator.vault.core.media.player.AudioTrack
import com.calculator.vault.core.media.player.RepeatMode
import com.calculator.vault.core.model.MediaType
import com.calculator.vault.core.ui.component.AudioArt
import com.calculator.vault.core.ui.component.BulkAction
import com.calculator.vault.core.ui.component.ConfirmBulkActionDialog
import com.calculator.vault.core.ui.component.DetailsDialog
import com.calculator.vault.core.ui.component.RenameDialog
import com.calculator.vault.core.ui.component.TransferProgressDialog
import com.calculator.vault.core.ui.mvi.CollectEffects
import com.calculator.vault.core.ui.util.AppToasts
import com.calculator.vault.core.ui.util.ExternalIntents
import com.calculator.vault.core.ui.util.formatDuration
import com.calculator.vault.feature.audios.R
import com.calculator.vault.feature.audios.presentation.components.PlayPauseButton
import com.calculator.vault.feature.audios.presentation.mvi.AudioPlayerEffect
import com.calculator.vault.feature.audios.presentation.mvi.AudioPlayerIntent
import com.calculator.vault.feature.audios.presentation.mvi.AudioPlayerState
import com.calculator.vault.feature.audios.presentation.mvi.AudioPlayerViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import com.calculator.vault.core.ui.R as UiR

@OptIn(UnstableApi::class)
@Composable
fun AudioPlayerRoute(
    itemId: String,
    onOpenEqualizer: () -> Unit,
    onClose: () -> Unit,
    viewModel: AudioPlayerViewModel = koinViewModel(key = "audio-$itemId") { parametersOf(itemId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val failed = stringResource(UiR.string.ui_error_generic)
    val trashed = stringResource(UiR.string.ui_moved_to_trash)
    viewModel.effects.CollectEffects { effect ->
        when (effect) {
            AudioPlayerEffect.OpenEqualizer -> onOpenEqualizer()
            is AudioPlayerEffect.ShareFiles -> ExternalIntents.share(context, effect.files, effect.mimeType)
            is AudioPlayerEffect.Done -> {
                AppToasts.show(if (effect.action == BulkAction.Delete) trashed else context.getString(UiR.string.ui_selected_unhidden, 1))
                onClose()
            }
            AudioPlayerEffect.Failed -> snackbar.showToast(failed, ToastKind.Error)
            AudioPlayerEffect.Close -> onClose()
        }
    }
    AudioPlayerScreen(state, viewModel::onIntent, snackbar)
}

@Composable
fun AudioPlayerScreen(state: AudioPlayerState, onIntent: (AudioPlayerIntent) -> Unit, snackbar: SnackbarHostState = remember { SnackbarHostState() }) {
    val playback = state.playback
    val track = playback.current
    Box(Modifier.fillMaxSize().background(VaultTheme.scheme.background)) {
        Column(Modifier.fillMaxSize()) {
            VaultTopBar(stringResource(R.string.audios_player_title), onNavigationClick = { onIntent(AudioPlayerIntent.Back) }) {
                Box {
                    VaultIconButton(VaultIcons.MoreVert, null, { onIntent(AudioPlayerIntent.OpenMenu) })
                    VaultDropdownMenu(state.menuOpen, { onIntent(AudioPlayerIntent.CloseMenu) }) {
                        VaultMenuItem(stringResource(UiR.string.ui_unhide), { onIntent(AudioPlayerIntent.Unhide) })
                        VaultMenuItem(stringResource(UiR.string.ui_share), { onIntent(AudioPlayerIntent.Share) })
                        VaultMenuItem(stringResource(UiR.string.ui_details), { onIntent(AudioPlayerIntent.Details) })
                        VaultMenuItem(stringResource(UiR.string.ui_rename), { onIntent(AudioPlayerIntent.Rename) })
                        VaultMenuItem(stringResource(UiR.string.ui_delete), { onIntent(AudioPlayerIntent.Delete) })
                    }
                }
            }
            if (isLandscape()) {
                Row(Modifier.fillMaxSize().navigationBarsPadding().padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
                    AudioArt(track?.id ?: "none", Modifier.fillMaxHeight(0.8f).aspectRatio(1f), size = 220.dp)
                    Spacer(Modifier.padding(16.dp))
                    Column(Modifier.weight(1f)) { Controls(playback, track, onIntent) }
                }
            } else {
                Column(
                    Modifier.fillMaxSize().navigationBarsPadding().padding(horizontal = 28.dp).padding(bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(Modifier.weight(0.4f))
                    Box(Modifier.widthIn(max = 300.dp).fillMaxWidth(0.82f).aspectRatio(1f)) {
                        AudioArt(track?.id ?: "none", Modifier.fillMaxSize(), size = 300.dp)
                    }
                    Spacer(Modifier.weight(0.6f))
                    Controls(playback, track, onIntent)
                }
            }
        }
        VaultSnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }
    if (state.queueOpen) {
        VaultBottomSheet(onDismissRequest = { onIntent(AudioPlayerIntent.Dismiss) }) {
            Text(stringResource(R.string.audios_queue), style = VaultTheme.text.dialogTitle, color = VaultTheme.colors.dialogTitle)
            playback.queue.forEachIndexed { index, item ->
                SettingsRow(item.title, subtitle = item.subtitle, trailing = RowTrailing.Check(index == playback.currentIndex), onClick = { onIntent(AudioPlayerIntent.PlayFromQueue(index)) })
            }
        }
    }
    val item = state.currentItem
    if (state.renaming && item != null) RenameDialog(item.name, { onIntent(AudioPlayerIntent.RenameConfirmed(it)) }, { onIntent(AudioPlayerIntent.Dismiss) })
    if (state.showDetails && item != null) DetailsDialog(item, null) { onIntent(AudioPlayerIntent.Dismiss) }
    state.confirm?.let { ConfirmBulkActionDialog(it, MediaType.AUDIO, 1, { onIntent(AudioPlayerIntent.Confirm) }, { onIntent(AudioPlayerIntent.Dismiss) }) }
    state.progress?.let { TransferProgressDialog(stringResource(UiR.string.ui_unhiding), it) }
}

@Composable
private fun Controls(playback: AudioPlaybackState, track: AudioTrack?, onIntent: (AudioPlayerIntent) -> Unit) {
    var dragging by remember { mutableStateOf(false) }
    var dragValue by remember { mutableFloatStateOf(0f) }
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(track?.title ?: "", style = VaultTheme.typography.headlineSmall, color = VaultTheme.scheme.onBackground, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
        Text(track?.subtitle ?: "", style = VaultTheme.typography.bodyMedium, color = VaultTheme.scheme.onSurfaceVariant)
        Spacer(Modifier.height(28.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            VaultIconButton(VaultIcons.Equalizer, stringResource(R.string.audios_equalizer), { onIntent(AudioPlayerIntent.Equalizer) }, tint = VaultTheme.scheme.onBackground)
            Row(verticalAlignment = Alignment.CenterVertically) {
                VaultIconButton(VaultIcons.Speed, stringResource(R.string.audios_speed), { onIntent(AudioPlayerIntent.Speed) }, tint = VaultTheme.scheme.onBackground)
                if (playback.speed != 1f) Text("${playback.speed}×", style = VaultTheme.typography.labelSmall, color = VaultTheme.scheme.primary)
            }
            VaultIconButton(VaultIcons.QueueMusic, stringResource(R.string.audios_queue), { onIntent(AudioPlayerIntent.OpenQueue) }, tint = VaultTheme.scheme.onBackground)
        }
        Spacer(Modifier.height(16.dp))
        val duration = playback.durationMs.coerceAtLeast(1L)
        val position = if (dragging) dragValue else playback.positionMs.toFloat() / duration
        Row(Modifier.fillMaxWidth()) {
            Text(formatDuration((position * duration).toLong()), style = VaultTheme.text.caption, color = VaultTheme.scheme.onSurfaceVariant)
            Spacer(Modifier.weight(1f))
            Text(formatDuration(playback.durationMs), style = VaultTheme.text.caption, color = VaultTheme.scheme.onSurfaceVariant)
        }
        VaultSlider(
            value = position.coerceIn(0f, 1f),
            onValueChange = { dragging = true; dragValue = it },
            onValueChangeFinished = {
                onIntent(AudioPlayerIntent.Seek((dragValue * duration).toLong()))
                dragging = false
            },
        )
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            VaultIconButton(VaultIcons.Shuffle, null, { onIntent(AudioPlayerIntent.Shuffle) }, tint = if (playback.shuffle) VaultTheme.scheme.primary else VaultTheme.scheme.onBackground)
            VaultIconButton(VaultIcons.SkipPrevious, null, { onIntent(AudioPlayerIntent.Previous) }, tint = VaultTheme.scheme.onBackground, iconSize = 28.dp)
            PlayPauseButton(playback.isPlaying, { onIntent(AudioPlayerIntent.PlayPause) })
            VaultIconButton(VaultIcons.SkipNext, null, { onIntent(AudioPlayerIntent.Next) }, tint = VaultTheme.scheme.onBackground, iconSize = 28.dp)
            VaultIconButton(
                if (playback.repeat == RepeatMode.ONE) VaultIcons.RepeatOne else VaultIcons.Repeat,
                null,
                { onIntent(AudioPlayerIntent.Repeat) },
                tint = if (playback.repeat != RepeatMode.OFF) VaultTheme.scheme.primary else VaultTheme.scheme.onBackground,
            )
        }
    }
}

@ScreenPreviews
@Composable
private fun AudioPlayerPreview() = VaultPreview {
    AudioPlayerScreen(
        AudioPlayerState(AudioPlaybackState(listOf(AudioTrack("1", "Late Night Notes", "Recorded")), 0, true, 134_000, 238_000)),
        onIntent = {},
    )
}
