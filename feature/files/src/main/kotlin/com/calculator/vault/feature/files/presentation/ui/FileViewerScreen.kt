package com.calculator.vault.feature.files.presentation.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.calculator.vault.core.designsystem.component.BarAction
import com.calculator.vault.core.designsystem.component.EmptyState
import com.calculator.vault.core.designsystem.component.FloatingActionBar
import com.calculator.vault.core.designsystem.component.LoadingContent
import com.calculator.vault.core.designsystem.component.ToastKind
import com.calculator.vault.core.designsystem.component.VaultButton
import com.calculator.vault.core.designsystem.component.VaultTopBar
import com.calculator.vault.core.designsystem.component.showToast
import com.calculator.vault.core.designsystem.icon.VaultIcons
import com.calculator.vault.core.designsystem.theme.VaultTheme
import com.calculator.vault.core.model.MediaType
import com.calculator.vault.core.model.VaultImageRequest
import com.calculator.vault.core.ui.component.BulkAction
import com.calculator.vault.core.ui.component.ConfirmBulkActionDialog
import com.calculator.vault.core.ui.component.FileTypeIcon
import com.calculator.vault.core.ui.component.TransferProgressDialog
import com.calculator.vault.core.ui.component.VaultScreen
import com.calculator.vault.core.ui.mvi.CollectEffects
import com.calculator.vault.core.ui.util.AppToasts
import com.calculator.vault.core.ui.util.ExternalIntents
import com.calculator.vault.feature.files.R
import com.calculator.vault.feature.files.presentation.mvi.FileContent
import com.calculator.vault.feature.files.presentation.mvi.FileViewerEffect
import com.calculator.vault.feature.files.presentation.mvi.FileViewerIntent
import com.calculator.vault.feature.files.presentation.mvi.FileViewerViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import com.calculator.vault.core.ui.R as UiR

@Composable
fun FileViewerRoute(itemId: String, onClose: () -> Unit, viewModel: FileViewerViewModel = koinViewModel(key = "file-$itemId") { parametersOf(itemId) }) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val failed = stringResource(UiR.string.ui_error_generic)
    val trashed = stringResource(UiR.string.ui_moved_to_trash)
    viewModel.effects.CollectEffects { effect ->
        when (effect) {
            is FileViewerEffect.ShareFiles -> ExternalIntents.share(context, effect.files, effect.mimeType)
            is FileViewerEffect.OpenWith -> ExternalIntents.openWith(context, effect.file, effect.mimeType)
            is FileViewerEffect.Done -> {
                AppToasts.show(if (effect.action == BulkAction.Delete) trashed else context.getString(UiR.string.ui_selected_unhidden, 1))
                onClose()
            }
            FileViewerEffect.Failed -> snackbar.showToast(failed, ToastKind.Error)
            FileViewerEffect.Close -> onClose()
        }
    }
    VaultScreen(
        snackbarHostState = snackbar,
        topBar = { VaultTopBar(state.item?.name ?: "", onNavigationClick = { viewModel.onIntent(FileViewerIntent.Back) }) },
        bottomBar = {
            FloatingActionBar(
                listOf(
                    BarAction(stringResource(UiR.string.ui_share), { viewModel.onIntent(FileViewerIntent.Share) }),
                    BarAction(stringResource(UiR.string.ui_delete), { viewModel.onIntent(FileViewerIntent.Delete) }),
                    BarAction(stringResource(UiR.string.ui_unhide), { viewModel.onIntent(FileViewerIntent.Unhide) }),
                ),
            )
        },
    ) {
        when (val content = state.content) {
            FileContent.Loading -> LoadingContent()
            is FileContent.Pdf -> PdfPages(content.pageCount, viewModel::renderPage)
            is FileContent.Text -> SelectionContainer {
                Text(
                    content.text + if (content.truncated) "\n\n…" else "",
                    style = VaultTheme.typography.bodyMedium.copy(fontSize = VaultTheme.text.rowTitle.fontSize),
                    color = VaultTheme.scheme.onSurface,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .widthIn(max = 760.dp)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                        .background(VaultTheme.scheme.surface)
                        .padding(16.dp)
                        .padding(bottom = 120.dp),
                )
            }
            FileContent.Image -> AsyncImage(
                VaultImageRequest(itemId),
                null,
                Modifier.fillMaxSize().padding(bottom = 100.dp),
                contentScale = ContentScale.Fit,
            )
            FileContent.Unsupported -> Column(
                Modifier.align(Alignment.Center).padding(24.dp).widthIn(max = 420.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                EmptyState(stringResource(R.string.files_unsupported_title), stringResource(R.string.files_unsupported_message)) {
                    state.item?.let { FileTypeIcon(it.name, it.mimeType, size = 88.dp) }
                }
                VaultButton(stringResource(R.string.files_open_with), { viewModel.onIntent(FileViewerIntent.OpenWith) }, leadingIcon = VaultIcons.Unhide)
            }
            FileContent.Error -> EmptyState(VaultIcons.Warning, stringResource(R.string.files_error_title), stringResource(UiR.string.ui_error_generic), Modifier.align(Alignment.Center))
        }
    }
    state.confirm?.let { ConfirmBulkActionDialog(it, MediaType.FILE, 1, { viewModel.onIntent(FileViewerIntent.Confirm) }, { viewModel.onIntent(FileViewerIntent.Dismiss) }) }
    state.progress?.let { TransferProgressDialog(stringResource(UiR.string.ui_unhiding), it) }
}

/** Lazily renders PDF pages at the screen width; only visible pages are rendered. */
@Composable
private fun PdfPages(pageCount: Int, render: suspend (Int, Int) -> Bitmap?) {
    BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        val pageWidthPx = with(LocalDensity.current) { minOf(maxWidth, 760.dp).roundToPx() }
        LazyColumn(
            Modifier.widthIn(max = 760.dp),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 130.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(pageCount) { index ->
                val bitmap by produceState<Bitmap?>(null, index, pageWidthPx) { value = render(index, pageWidthPx) }
                Box(Modifier.fillMaxWidth().aspectRatio(bitmap?.let { it.width.toFloat() / it.height } ?: 0.707f).shadow(2.dp).background(androidx.compose.ui.graphics.Color.White)) {
                    bitmap?.let { Image(it.asImageBitmap(), null, Modifier.fillMaxSize()) }
                }
            }
        }
    }
}
