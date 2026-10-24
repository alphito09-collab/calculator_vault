package com.calculator.vault.feature.videos.presentation.ui

import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.calculator.vault.core.designsystem.component.ToastKind
import com.calculator.vault.core.designsystem.component.VaultIconButton
import com.calculator.vault.core.designsystem.component.VaultSnackbarHost
import com.calculator.vault.core.designsystem.component.showToast
import com.calculator.vault.core.designsystem.icon.VaultIcons
import com.calculator.vault.core.designsystem.theme.VaultTheme
import com.calculator.vault.core.model.MediaType
import com.calculator.vault.core.ui.component.BulkAction
import com.calculator.vault.core.ui.component.ConfirmBulkActionDialog
import com.calculator.vault.core.ui.mvi.CollectEffects
import com.calculator.vault.core.ui.util.AppToasts
import com.calculator.vault.core.ui.util.ExternalIntents
import com.calculator.vault.core.ui.util.ImmersiveMode
import com.calculator.vault.feature.videos.presentation.mvi.VideoPlayerEffect
import com.calculator.vault.feature.videos.presentation.mvi.VideoPlayerIntent
import com.calculator.vault.feature.videos.presentation.mvi.VideoPlayerViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import com.calculator.vault.core.designsystem.component.VaultTopBar
import com.calculator.vault.core.ui.R as UiR

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerRoute(
    itemId: String,
    onClose: () -> Unit,
    viewModel: VideoPlayerViewModel = koinViewModel(key = "video-$itemId") { parametersOf(itemId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val failed = stringResource(UiR.string.ui_error_generic)
    val trashed = stringResource(UiR.string.ui_moved_to_trash)
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { viewModel.onIntent(VideoPlayerIntent.Pause) }
    viewModel.effects.CollectEffects { effect ->
        when (effect) {
            is VideoPlayerEffect.ShareFiles -> ExternalIntents.share(context, effect.files, effect.mimeType)
            VideoPlayerEffect.Deleted -> {
                AppToasts.show(trashed)
                onClose()
            }
            VideoPlayerEffect.Failed -> snackbar.showToast(failed, ToastKind.Error)
            VideoPlayerEffect.Close -> onClose()
        }
    }
    BackHandler { onClose() }
    ImmersiveMode(enabled = !state.chromeVisible)
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = viewModel.player
                    setShowNextButton(true)
                    setShowPreviousButton(true)
                    setControllerVisibilityListener(PlayerView.ControllerVisibilityListener { visibility ->
                        viewModel.onIntent(VideoPlayerIntent.ControllerVisibility(visibility == android.view.View.VISIBLE))
                    })
                }
            },
            onRelease = { it.player = null },
            modifier = Modifier.fillMaxSize(),
        )
        AnimatedVisibility(state.chromeVisible, enter = fadeIn(), exit = fadeOut(), modifier = Modifier.align(Alignment.TopCenter)) {
            Box(Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)))) {
                CompositionLocalProvider(LocalContentColor provides Color.White) {
                    VaultTopBar(state.current?.name ?: "", onNavigationClick = onClose) {
                        VaultIconButton(VaultIcons.Share, stringResource(UiR.string.ui_share), { viewModel.onIntent(VideoPlayerIntent.Share) }, tint = Color.White)
                        VaultIconButton(VaultIcons.Delete, stringResource(UiR.string.ui_delete), { viewModel.onIntent(VideoPlayerIntent.Delete) }, tint = Color.White)
                    }
                }
            }
        }
        VaultSnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }
    if (state.confirmDelete) {
        ConfirmBulkActionDialog(BulkAction.Delete, MediaType.VIDEO, 1, { viewModel.onIntent(VideoPlayerIntent.ConfirmDelete) }, { viewModel.onIntent(VideoPlayerIntent.DismissDelete) })
    }
}
