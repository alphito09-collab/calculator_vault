package com.calculator.vault.feature.trash.presentation.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calculator.vault.core.common.VaultConstants
import com.calculator.vault.core.designsystem.component.BarAction
import com.calculator.vault.core.designsystem.component.EmptyState
import com.calculator.vault.core.designsystem.component.FloatingActionBar
import com.calculator.vault.core.designsystem.component.LoadingContent
import com.calculator.vault.core.designsystem.component.SelectionTopBar
import com.calculator.vault.core.designsystem.component.ToastKind
import com.calculator.vault.core.designsystem.component.VaultDialog
import com.calculator.vault.core.designsystem.component.VaultTextButton
import com.calculator.vault.core.designsystem.component.VaultTopBar
import com.calculator.vault.core.designsystem.component.showToast
import com.calculator.vault.core.designsystem.icon.VaultIcons
import com.calculator.vault.core.designsystem.preview.ScreenPreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultRadius
import com.calculator.vault.core.designsystem.theme.VaultTheme
import com.calculator.vault.core.model.MediaType
import com.calculator.vault.core.model.VaultItem
import com.calculator.vault.core.model.VaultThumbnailRequest
import com.calculator.vault.core.ui.component.ConfirmBulkActionDialog
import com.calculator.vault.core.ui.component.MediaThumbnail
import com.calculator.vault.core.ui.component.VaultItemRow
import com.calculator.vault.core.ui.component.VaultScreen
import com.calculator.vault.core.ui.mvi.CollectEffects
import com.calculator.vault.core.ui.util.formatBytes
import com.calculator.vault.feature.trash.R
import com.calculator.vault.feature.trash.presentation.mvi.TrashEffect
import com.calculator.vault.feature.trash.presentation.mvi.TrashIntent
import com.calculator.vault.feature.trash.presentation.mvi.TrashState
import com.calculator.vault.feature.trash.presentation.mvi.TrashViewModel
import org.koin.androidx.compose.koinViewModel
import java.util.concurrent.TimeUnit
import com.calculator.vault.core.ui.R as UiR

@Composable
fun TrashRoute(onClose: () -> Unit, viewModel: TrashViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val failed = stringResource(UiR.string.ui_error_generic)
    viewModel.effects.CollectEffects { effect ->
        when (effect) {
            is TrashEffect.Restored -> snackbar.showToast(context.resources.getQuantityString(R.plurals.trash_restored, effect.count, effect.count))
            is TrashEffect.Deleted -> snackbar.showToast(context.resources.getQuantityString(R.plurals.trash_deleted, effect.count, effect.count))
            TrashEffect.Failed -> snackbar.showToast(failed, ToastKind.Error)
            TrashEffect.Close -> onClose()
        }
    }
    BackHandler { viewModel.onIntent(TrashIntent.Back) }
    TrashScreen(state, viewModel::onIntent, snackbar)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TrashScreen(state: TrashState, onIntent: (TrashIntent) -> Unit, snackbar: SnackbarHostState = remember { SnackbarHostState() }) {
    VaultScreen(
        snackbarHostState = snackbar,
        topBar = {
            if (state.selecting) {
                SelectionTopBar(state.selected.size, state.selected.size == state.items.size, { onIntent(TrashIntent.ClearSelection) }, { onIntent(TrashIntent.ToggleSelectAll) })
            } else {
                VaultTopBar(stringResource(R.string.trash_title), onNavigationClick = { onIntent(TrashIntent.Back) }) {
                    if (state.items.isNotEmpty()) VaultTextButton(stringResource(R.string.trash_empty_action), { onIntent(TrashIntent.EmptyTrash) }, color = VaultTheme.scheme.error)
                }
            }
        },
        bottomVisible = state.selecting,
        bottomBar = {
            FloatingActionBar(
                listOf(
                    BarAction(stringResource(UiR.string.ui_restore), { onIntent(TrashIntent.Restore) }),
                    BarAction(stringResource(R.string.trash_delete_forever), { onIntent(TrashIntent.DeleteForever) }, destructive = true),
                ),
            )
        },
    ) {
        when {
            state.loading -> LoadingContent()
            state.items.isEmpty() -> EmptyState(VaultIcons.Trash, stringResource(R.string.trash_empty_title), stringResource(R.string.trash_empty_message), Modifier.align(Alignment.Center))
            else -> LazyVerticalGrid(
                columns = GridCells.Adaptive(104.dp),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 130.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        stringResource(R.string.trash_retention_note, VaultConstants.TRASH_RETENTION_DAYS),
                        style = VaultTheme.typography.bodySmall,
                        color = VaultTheme.scheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                }
                items(
                    state.items,
                    key = { it.id },
                    span = { if (it.type == MediaType.PHOTO || it.type == MediaType.VIDEO) GridItemSpan(1) else GridItemSpan(maxLineSpan) },
                ) { item ->
                    val selected = item.id in state.selected
                    val daysLeft = daysLeft(item)
                    if (item.type == MediaType.PHOTO || item.type == MediaType.VIDEO) {
                        Box(Modifier.animateItem()) {
                            MediaThumbnail(
                                itemId = item.id,
                                model = if (item.hasThumbnail) VaultThumbnailRequest(item.id) else null,
                                durationMs = if (item.type == MediaType.VIDEO) item.durationMs ?: 0 else null,
                                selectionMode = state.selecting,
                                selected = selected,
                                modifier = Modifier.clip(VaultRadius.Thumbnail).combinedClickable(onLongClick = { onIntent(TrashIntent.Toggle(item)) }) { onIntent(TrashIntent.Toggle(item)) },
                            )
                            Text(
                                pluralStringResource(R.plurals.trash_days_left, daysLeft, daysLeft),
                                style = VaultTheme.text.caption,
                                color = Color.White,
                                modifier = Modifier.align(Alignment.BottomEnd).padding(6.dp).clip(VaultRadius.Pill).background(Color.Black.copy(alpha = 0.5f)).padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    } else {
                        VaultItemRow(
                            item = item,
                            subtitle = "${formatBytes(item.sizeBytes)} • ${pluralStringResource(R.plurals.trash_days_left, daysLeft, daysLeft)}",
                            onClick = { onIntent(TrashIntent.Toggle(item)) },
                            onLongClick = { onIntent(TrashIntent.Toggle(item)) },
                            selectionMode = true,
                            selected = selected,
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
    }
    state.confirm?.let { ConfirmBulkActionDialog(it, null, state.selected.size, { onIntent(TrashIntent.Confirm) }, { onIntent(TrashIntent.Dismiss) }) }
    if (state.confirmEmpty) {
        VaultDialog(
            title = stringResource(R.string.trash_empty_confirm_title),
            message = stringResource(R.string.trash_empty_confirm_message, state.items.size),
            confirmText = stringResource(R.string.trash_empty_action),
            onConfirm = { onIntent(TrashIntent.Confirm) },
            onDismissRequest = { onIntent(TrashIntent.Dismiss) },
            destructive = true,
        )
    }
}

private fun daysLeft(item: VaultItem): Int {
    val trashedAt = item.trashedAt ?: return VaultConstants.TRASH_RETENTION_DAYS
    val elapsed = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - trashedAt).toInt()
    return (VaultConstants.TRASH_RETENTION_DAYS - elapsed).coerceAtLeast(0)
}

@ScreenPreviews
@Composable
private fun TrashPreview() = VaultPreview {
    val now = System.currentTimeMillis()
    val items = (1..5).map { VaultItem("$it", MediaType.PHOTO, "IMG_$it.jpg", "image/jpeg", 1, null, null, 0, 0, trashedAt = now - it * 86_400_000L) } +
        VaultItem("f", MediaType.FILE, "Tax_2025.pdf", "application/pdf", 610_000, null, null, 0, 0, trashedAt = now)
    TrashScreen(TrashState(loading = false, items = items, selected = setOf("2")), onIntent = {})
}
