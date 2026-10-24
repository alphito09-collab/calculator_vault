package com.calculator.vault.core.ui.gallery

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.calculator.vault.core.designsystem.component.BarAction
import com.calculator.vault.core.designsystem.component.EmptyState
import com.calculator.vault.core.designsystem.component.FloatingActionBar
import com.calculator.vault.core.designsystem.component.LoadingContent
import com.calculator.vault.core.designsystem.component.RowDivider
import com.calculator.vault.core.designsystem.component.SelectionTopBar
import com.calculator.vault.core.designsystem.component.ToastKind
import com.calculator.vault.core.designsystem.component.VaultButton
import com.calculator.vault.core.designsystem.component.VaultDropdownMenu
import com.calculator.vault.core.designsystem.component.VaultIconButton
import com.calculator.vault.core.designsystem.component.VaultMenuItem
import com.calculator.vault.core.designsystem.component.VaultTopBar
import com.calculator.vault.core.designsystem.component.showToast
import com.calculator.vault.core.designsystem.icon.VaultIcons
import com.calculator.vault.core.model.GridSize
import com.calculator.vault.core.ui.R
import com.calculator.vault.core.ui.component.BulkAction
import com.calculator.vault.core.ui.component.ConfirmBulkActionDialog
import com.calculator.vault.core.ui.component.TransferProgressDialog
import com.calculator.vault.core.ui.component.VaultScreen
import com.calculator.vault.core.ui.mvi.CollectEffects
import com.calculator.vault.core.ui.util.ExternalIntents
import com.calculator.vault.core.ui.util.typeLabel
import com.calculator.vault.core.ui.util.typeTitle
import com.calculator.vault.core.model.VaultItem
import kotlinx.coroutines.flow.Flow

/** Handles the effects every gallery shares; [onOpen]/[onImport]/[onBack] are navigation. */
@Composable
fun GalleryEffects(
    effects: Flow<GalleryEffect>,
    snackbar: SnackbarHostState,
    onOpen: (VaultItem) -> Unit,
    onImport: () -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val trashed = stringResource(R.string.ui_moved_to_trash)
    val failed = stringResource(R.string.ui_error_generic)
    effects.CollectEffects { effect ->
        when (effect) {
            is GalleryEffect.Open -> onOpen(effect.item)
            is GalleryEffect.ShareFiles -> ExternalIntents.share(context, effect.files, effect.mimeType)
            is GalleryEffect.Done -> snackbar.showToast(
                if (effect.action == BulkAction.Delete) trashed else context.getString(R.string.ui_selected_unhidden, effect.count),
            )
            GalleryEffect.Failed -> snackbar.showToast(failed, ToastKind.Error)
            GalleryEffect.Import -> onImport()
            GalleryEffect.Back -> onBack()
        }
    }
}

/**
 * Chrome shared by Photos/Videos/Audio/Files: title or selection bar, overflow menu with
 * layout options, empty state, "Import …" button or Share/Delete/Unhide bar, dialogs.
 */
@Composable
fun GalleryScaffold(
    state: GalleryState,
    onIntent: (GalleryIntent) -> Unit,
    @DrawableRes emptyIcon: Int,
    emptyTitle: String,
    emptyMessage: String,
    importLabel: String,
    snackbar: SnackbarHostState = remember { SnackbarHostState() },
    showLayoutOptions: Boolean = true,
    extraMenuItems: (@Composable () -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    BackHandler(enabled = state.selecting) { onIntent(GalleryIntent.ClearSelection) }
    VaultScreen(
        snackbarHostState = snackbar,
        topBar = {
            AnimatedContent(state.selecting, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "galleryTopBar") { selecting ->
                if (selecting) {
                    SelectionTopBar(state.selected.size, state.allSelected, { onIntent(GalleryIntent.ClearSelection) }, { onIntent(GalleryIntent.ToggleSelectAll) })
                } else {
                    VaultTopBar(typeTitle(state.type), onNavigationClick = { onIntent(GalleryIntent.Back) }) {
                        if (state.items.isNotEmpty()) {
                            Box {
                                VaultIconButton(VaultIcons.MoreVert, stringResource(com.calculator.vault.core.designsystem.R.string.ds_cd_more), { onIntent(GalleryIntent.OpenMenu) })
                                VaultDropdownMenu(state.menuOpen, { onIntent(GalleryIntent.CloseMenu) }) {
                                    VaultMenuItem(stringResource(R.string.ui_select_items), {
                                        onIntent(GalleryIntent.CloseMenu)
                                        onIntent(GalleryIntent.ToggleSelectAll)
                                    })
                                    extraMenuItems?.invoke()
                                    if (showLayoutOptions) {
                                        RowDivider()
                                        GridSize.entries.forEach { size ->
                                            VaultMenuItem(
                                                stringResource(size.labelRes()) + if (size == state.gridSize) "  ✓" else "",
                                                { onIntent(GalleryIntent.SetGridSize(size)); onIntent(GalleryIntent.CloseMenu) },
                                            )
                                        }
                                        VaultMenuItem(
                                            stringResource(R.string.ui_group_by_date) + if (state.groupByDate) "  ✓" else "",
                                            { onIntent(GalleryIntent.SetGroupByDate(!state.groupByDate)); onIntent(GalleryIntent.CloseMenu) },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            AnimatedContent(state.selecting, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "galleryBottom") { selecting ->
                if (selecting) {
                    FloatingActionBar(
                        listOf(
                            BarAction(stringResource(R.string.ui_share), { onIntent(GalleryIntent.Share) }),
                            BarAction(stringResource(R.string.ui_delete), { onIntent(GalleryIntent.Delete) }),
                            BarAction(stringResource(R.string.ui_unhide), { onIntent(GalleryIntent.Unhide) }),
                        ),
                    )
                } else {
                    VaultButton(importLabel, { onIntent(GalleryIntent.Import) }, leadingIcon = VaultIcons.Plus)
                }
            }
        },
    ) {
        when {
            state.loading -> LoadingContent()
            state.items.isEmpty() -> EmptyState(emptyIcon, emptyTitle, emptyMessage, Modifier.align(Alignment.Center))
            else -> content()
        }
    }
    state.confirm?.let { action ->
        ConfirmBulkActionDialog(action, state.type, state.selected.size, { onIntent(GalleryIntent.Confirm) }, { onIntent(GalleryIntent.DismissConfirm) })
    }
    state.progress?.let { TransferProgressDialog(stringResource(R.string.ui_unhiding), it) }
}

private fun GridSize.labelRes(): Int = when (this) {
    GridSize.LARGE -> R.string.ui_grid_large
    GridSize.MEDIUM -> R.string.ui_grid_medium
    GridSize.SMALL -> R.string.ui_grid_small
}

