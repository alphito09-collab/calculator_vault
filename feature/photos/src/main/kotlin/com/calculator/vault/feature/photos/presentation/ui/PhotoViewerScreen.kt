package com.calculator.vault.feature.photos.presentation.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calculator.vault.core.designsystem.component.BarAction
import com.calculator.vault.core.designsystem.component.FloatingActionBar
import com.calculator.vault.core.designsystem.component.ToastKind
import com.calculator.vault.core.designsystem.component.VaultDropdownMenu
import com.calculator.vault.core.designsystem.component.VaultIconButton
import com.calculator.vault.core.designsystem.component.VaultMenuItem
import com.calculator.vault.core.designsystem.component.VaultSnackbarHost
import com.calculator.vault.core.designsystem.component.VaultTopBar
import com.calculator.vault.core.designsystem.component.showToast
import com.calculator.vault.core.designsystem.icon.VaultIcons
import com.calculator.vault.core.designsystem.preview.ScreenPreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultTheme
import com.calculator.vault.core.model.MediaType
import com.calculator.vault.core.model.VaultItem
import com.calculator.vault.core.ui.component.BottomFade
import com.calculator.vault.core.ui.component.ConfirmBulkActionDialog
import com.calculator.vault.core.ui.component.DetailsDialog
import com.calculator.vault.core.ui.component.RenameDialog
import com.calculator.vault.core.ui.component.TransferProgressDialog
import com.calculator.vault.core.ui.mvi.CollectEffects
import com.calculator.vault.core.ui.util.AppToasts
import com.calculator.vault.core.ui.util.ExternalIntents
import com.calculator.vault.core.ui.util.ImmersiveMode
import com.calculator.vault.feature.photos.R
import com.calculator.vault.feature.photos.presentation.components.IntervalPickerDialog
import com.calculator.vault.feature.photos.presentation.components.ZoomableImage
import com.calculator.vault.feature.photos.presentation.mvi.PhotoViewerEffect
import com.calculator.vault.feature.photos.presentation.mvi.PhotoViewerIntent
import com.calculator.vault.feature.photos.presentation.mvi.PhotoViewerState
import com.calculator.vault.feature.photos.presentation.mvi.PhotoViewerViewModel
import com.calculator.vault.core.ui.component.BulkAction
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import com.calculator.vault.core.ui.R as UiR

@Composable
fun PhotoViewerRoute(
    itemId: String,
    slideshow: Boolean,
    onClose: () -> Unit,
    viewModel: PhotoViewerViewModel = koinViewModel(key = "viewer-$itemId-$slideshow") { parametersOf(itemId, slideshow) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val failed = stringResource(UiR.string.ui_error_generic)
    val trashed = stringResource(UiR.string.ui_moved_to_trash)
    viewModel.effects.CollectEffects { effect ->
        when (effect) {
            is PhotoViewerEffect.ShareFiles -> ExternalIntents.share(context, effect.files, effect.mimeType)
            is PhotoViewerEffect.Done -> {
                AppToasts.show(if (effect.action == BulkAction.Delete) trashed else context.getString(UiR.string.ui_selected_unhidden, 1))
                if (state.items.size <= 1) onClose()
            }
            PhotoViewerEffect.Failed -> snackbar.showToast(failed, ToastKind.Error)
            PhotoViewerEffect.Close -> onClose()
        }
    }
    BackHandler { if (state.slideshow) viewModel.onIntent(PhotoViewerIntent.StopSlideshow) else onClose() }
    PhotoViewerScreen(state, viewModel::onIntent, snackbar)
}

@Composable
fun PhotoViewerScreen(state: PhotoViewerState, onIntent: (PhotoViewerIntent) -> Unit, snackbar: SnackbarHostState = remember { SnackbarHostState() }) {
    ImmersiveMode(enabled = !state.chromeVisible)
    val background by animateColorAsState(if (state.chromeVisible) VaultTheme.scheme.background else Color.Black, label = "viewerBg")
    Box(Modifier.fillMaxSize().background(background)) {
        if (state.items.isNotEmpty() && state.index >= 0) {
            val pager = rememberPagerState(initialPage = state.index) { state.items.size }
            LaunchedEffect(pager) { snapshotFlow { pager.currentPage }.collect { onIntent(PhotoViewerIntent.PageChanged(it)) } }
            LaunchedEffect(state.index, state.items.size) {
                if (pager.currentPage != state.index && state.index in state.items.indices) pager.scrollToPage(state.index)
            }
            if (state.slideshow) {
                LaunchedEffect(pager.currentPage, state.intervalSeconds) {
                    delay(state.intervalSeconds * 1_000L)
                    pager.animateScrollToPage((pager.currentPage + 1) % state.items.size)
                }
            }
            HorizontalPager(pager, Modifier.fillMaxSize(), key = { state.items[it].id }, beyondViewportPageCount = 1) { page ->
                val item = state.items[page]
                ZoomableImage(item.id, item.hasThumbnail, onTap = { onIntent(PhotoViewerIntent.ToggleChrome) })
            }
        }
        AnimatedVisibility(
            state.chromeVisible,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier.align(Alignment.TopCenter),
        ) {
            Box(Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(VaultTheme.scheme.background, VaultTheme.scheme.background.copy(alpha = 0.6f), Color.Transparent)))) {
                VaultTopBar(stringResource(UiR.string.ui_title_photos), onNavigationClick = { onIntent(PhotoViewerIntent.Back) }) {
                    Box {
                        VaultIconButton(VaultIcons.MoreVert, null, { onIntent(PhotoViewerIntent.OpenMenu) })
                        VaultDropdownMenu(state.menuOpen, { onIntent(PhotoViewerIntent.CloseMenu) }) {
                            VaultMenuItem(stringResource(UiR.string.ui_rename), { onIntent(PhotoViewerIntent.Rename) })
                            VaultMenuItem(stringResource(UiR.string.ui_slideshow), { onIntent(PhotoViewerIntent.Slideshow) })
                            VaultMenuItem(stringResource(UiR.string.ui_details), { onIntent(PhotoViewerIntent.Details) })
                        }
                    }
                }
            }
        }
        AnimatedVisibility(
            state.chromeVisible,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            BottomFade {
                FloatingActionBar(
                    listOf(
                        BarAction(stringResource(UiR.string.ui_share), { onIntent(PhotoViewerIntent.Share) }),
                        BarAction(stringResource(UiR.string.ui_delete), { onIntent(PhotoViewerIntent.Delete) }),
                        BarAction(stringResource(UiR.string.ui_unhide), { onIntent(PhotoViewerIntent.Unhide) }),
                    ),
                )
            }
        }
        VaultSnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }
    val current = state.current
    if (state.renaming && current != null) {
        RenameDialog(current.name, { onIntent(PhotoViewerIntent.RenameConfirmed(it)) }, { onIntent(PhotoViewerIntent.DismissDialogs) })
    }
    if (state.showDetails && current != null) {
        DetailsDialog(current, currentPath = null, onDismiss = { onIntent(PhotoViewerIntent.DismissDialogs) })
    }
    if (state.pickingInterval) {
        IntervalPickerDialog(state.intervalSeconds, { onIntent(PhotoViewerIntent.StartSlideshow(it)) }, { onIntent(PhotoViewerIntent.DismissDialogs) })
    }
    state.confirm?.let { action ->
        ConfirmBulkActionDialog(action, MediaType.PHOTO, 1, { onIntent(PhotoViewerIntent.Confirm) }, { onIntent(PhotoViewerIntent.DismissDialogs) })
    }
    state.progress?.let { TransferProgressDialog(stringResource(UiR.string.ui_unhiding), it) }
}

@ScreenPreviews
@Composable
private fun PhotoViewerPreview() = VaultPreview {
    PhotoViewerScreen(
        PhotoViewerState(items = listOf(VaultItem("1", MediaType.PHOTO, "IMG_1.jpg", "image/jpeg", 1, null, null, 0, 0)), index = 0, menuOpen = false),
        onIntent = {},
    )
}
