package com.calculator.vault.feature.importer.presentation.ui

import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calculator.vault.core.common.session.LockGuard
import com.calculator.vault.core.designsystem.component.EmptyState
import com.calculator.vault.core.designsystem.component.FilterChipRow
import com.calculator.vault.core.designsystem.component.LoadingContent
import com.calculator.vault.core.designsystem.component.NavigationIcon
import com.calculator.vault.core.designsystem.component.SelectionTopBar
import com.calculator.vault.core.designsystem.component.ToastKind
import com.calculator.vault.core.designsystem.component.VaultButton
import com.calculator.vault.core.designsystem.component.VaultTextButton
import com.calculator.vault.core.designsystem.component.VaultTopBar
import com.calculator.vault.core.designsystem.icon.VaultIcons
import com.calculator.vault.core.designsystem.preview.ScreenPreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultRadius
import com.calculator.vault.core.designsystem.theme.VaultTheme
import com.calculator.vault.core.model.DeviceAlbum
import com.calculator.vault.core.model.DeviceMedia
import com.calculator.vault.core.model.MediaType
import com.calculator.vault.core.ui.component.BulkAction
import com.calculator.vault.core.ui.util.albumLabel
import com.calculator.vault.core.ui.component.ConfirmBulkActionDialog
import com.calculator.vault.core.ui.component.MediaThumbnail
import com.calculator.vault.core.ui.component.TransferProgressDialog
import com.calculator.vault.core.ui.component.VaultScreen
import com.calculator.vault.core.ui.mvi.CollectEffects
import com.calculator.vault.core.ui.util.AppToasts
import com.calculator.vault.core.ui.util.ExternalIntents
import com.calculator.vault.core.ui.util.typeLabel
import com.calculator.vault.core.ui.util.typeTitle
import com.calculator.vault.feature.importer.R
import com.calculator.vault.feature.importer.presentation.components.BrowseStorageButton
import com.calculator.vault.feature.importer.presentation.components.DeviceMediaRow
import com.calculator.vault.feature.importer.presentation.mvi.ImporterEffect
import com.calculator.vault.feature.importer.presentation.mvi.ImporterIntent
import com.calculator.vault.feature.importer.presentation.mvi.ImporterState
import com.calculator.vault.feature.importer.presentation.mvi.ImporterViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import com.calculator.vault.core.ui.R as UiR

@Composable
fun ImporterRoute(type: MediaType?, onClose: () -> Unit, viewModel: ImporterViewModel = koinViewModel(key = "importer-$type") { parametersOf(type) }) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val doneTemplate = UiR.string.ui_selected_hidden
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = LocalActivity.current
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        // Denied without a rationale means "don't ask again": the prompt won't show any more.
        val permanentlyDenied = result.isNotEmpty() && result.values.none { it } && activity != null &&
            result.keys.none { ActivityCompat.shouldShowRequestPermissionRationale(activity, it) }
        viewModel.onIntent(ImporterIntent.PermissionResult(permanentlyDenied))
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.onIntent(ImporterIntent.Resumed) }
    val documentLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isNotEmpty()) viewModel.onIntent(ImporterIntent.DocumentsPicked(uris.map { it.toString() }))
    }
    viewModel.effects.CollectEffects { effect ->
        when (effect) {
            is ImporterEffect.Done -> {
                if (effect.hidden > 0) AppToasts.show(context.getString(doneTemplate, effect.hidden))
                if (effect.failed > 0) AppToasts.show(context.getString(R.string.importer_some_failed, effect.failed), ToastKind.Error)
                onClose()
            }
            is ImporterEffect.RequestPermission -> {
                LockGuard.allowExternalLaunch()
                permissionLauncher.launch(effect.permissions.toTypedArray())
            }
            ImporterEffect.OpenAppSettings -> ExternalIntents.openAppDetails(context)
            ImporterEffect.OpenAllFilesSettings -> ExternalIntents.openAllFilesAccessSettings(context)
            ImporterEffect.Close -> onClose()
        }
    }
    ImporterScreen(
        state = state,
        onIntent = viewModel::onIntent,
        onBrowse = {
            LockGuard.allowExternalLaunch()
            documentLauncher.launch(arrayOf(mimeFor(state.filter)))
        },
    )
}

private fun mimeFor(type: MediaType?): String = when (type) {
    MediaType.PHOTO -> "image/*"
    MediaType.VIDEO -> "video/*"
    MediaType.AUDIO -> "audio/*"
    else -> "*/*"
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImporterScreen(state: ImporterState, onIntent: (ImporterIntent) -> Unit, onBrowse: () -> Unit) {
    val count = state.selected.size
    val label = state.selectionType?.let { typeLabel(it, count) } ?: stringResource(UiR.string.ui_type_items)
    VaultScreen(
        topBar = {
            if (count > 0) {
                SelectionTopBar(count, state.allSelected, onClose = { onIntent(ImporterIntent.Close) }, onToggleSelectAll = { onIntent(ImporterIntent.ToggleSelectAll) })
            } else {
                VaultTopBar(
                    title = when {
                        state.lockedType == MediaType.PHOTO || state.lockedType == MediaType.VIDEO -> stringResource(R.string.importer_gallery)
                        state.lockedType != null -> typeTitle(state.lockedType)
                        else -> stringResource(R.string.importer_all)
                    },
                    navigationIcon = NavigationIcon.Close,
                    onNavigationClick = { onIntent(ImporterIntent.Close) },
                ) {
                    VaultTextButton(stringResource(com.calculator.vault.core.designsystem.R.string.ds_select_all), { onIntent(ImporterIntent.ToggleSelectAll) }, small = true)
                }
            }
        },
        bottomBar = {
            VaultButton(
                text = if (count == 0) stringResource(UiR.string.ui_hide) else stringResource(R.string.importer_hide_n, count, label.replaceFirstChar { it.uppercase() }),
                onClick = { onIntent(ImporterIntent.Hide) },
                enabled = count > 0,
            )
        },
    ) {
        Column(Modifier.fillMaxSize()) {
            if (state.lockedType == null) {
                FilterChipRow(
                    options = listOf<MediaType?>(null) + MediaType.entries,
                    selected = state.filter,
                    label = { if (it == null) stringResource(R.string.importer_all) else typeTitle(it) },
                    onSelect = { onIntent(ImporterIntent.Filter(it)) },
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
            when {
                state.loading -> LoadingContent()
                else -> LazyVerticalGrid(
                    columns = GridCells.Adaptive(104.dp),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 130.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }, key = "browse") {
                        BrowseStorageButton(stringResource(R.string.importer_browse), onBrowse, Modifier.padding(bottom = 8.dp))
                    }
                    if (state.albums.isEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }, key = "empty") {
                            Column(Modifier.padding(top = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                EmptyState(
                                    icon = VaultIcons.Folder,
                                    title = stringResource(R.string.importer_empty_title),
                                    message = stringResource(if (state.needsPermission) R.string.importer_needs_permission else R.string.importer_empty_message),
                                )
                                if (state.needsPermission) {
                                    VaultTextButton(stringResource(R.string.importer_allow_access), { onIntent(ImporterIntent.AllowAccess) })
                                }
                            }
                        }
                    }
                    state.albums.forEach { album ->
                        item(span = { GridItemSpan(maxLineSpan) }, key = "album-${album.name}", contentType = "header") {
                            Text(
                                albumLabel(album.name),
                                style = VaultTheme.typography.titleSmall.copy(fontSize = VaultTheme.typography.bodyLarge.fontSize),
                                color = VaultTheme.scheme.onBackground,
                                modifier = Modifier.padding(top = 10.dp),
                            )
                        }
                        items(
                            album.items,
                            key = { it.uri },
                            span = { if (it.type == MediaType.PHOTO || it.type == MediaType.VIDEO) GridItemSpan(1) else GridItemSpan(maxLineSpan) },
                            contentType = { it.type },
                        ) { item ->
                            val selected = item.uri in state.selected
                            if (item.type == MediaType.PHOTO || item.type == MediaType.VIDEO) {
                                MediaThumbnail(
                                    itemId = item.uri,
                                    model = item.uri,
                                    durationMs = if (item.type == MediaType.VIDEO) item.durationMs ?: 0L else null,
                                    selectionMode = true,
                                    selected = selected,
                                    modifier = Modifier.clip(VaultRadius.Thumbnail).combinedClickable { onIntent(ImporterIntent.Toggle(item)) },
                                )
                            } else {
                                DeviceMediaRow(item, selected, { onIntent(ImporterIntent.Toggle(item)) })
                            }
                        }
                    }
                }
            }
        }
    }
    if (state.confirmHide) {
        ConfirmBulkActionDialog(
            action = BulkAction.Hide,
            type = state.selectionType,
            count = count,
            onConfirm = { onIntent(ImporterIntent.ConfirmHide) },
            onDismiss = { onIntent(ImporterIntent.DismissHide) },
        )
    }
    state.progress?.let { TransferProgressDialog(stringResource(UiR.string.ui_hiding), it) }
}

@ScreenPreviews
@Composable
private fun ImporterPreview() = VaultPreview {
    val items = (1..7).map { DeviceMedia("content://media/$it", MediaType.PHOTO, "IMG_$it.jpg", "image/jpeg", 1000, "Camera", 0) }
    ImporterScreen(
        ImporterState(
            loading = false,
            albums = listOf(DeviceAlbum("Camera", items.take(3)), DeviceAlbum("WhatsApp Images", items.drop(3))),
            selected = items.take(2).associateBy { it.uri },
        ),
        onIntent = {},
        onBrowse = {},
    )
}

@ScreenPreviews
@Composable
private fun ImporterNeedsAccessPreview() = VaultPreview {
    ImporterScreen(ImporterState(lockedType = MediaType.VIDEO, filter = MediaType.VIDEO, loading = false, needsPermission = true), onIntent = {}, onBrowse = {})
}

@ScreenPreviews
@Composable
private fun ImporterNothingOnDevicePreview() = VaultPreview {
    ImporterScreen(ImporterState(lockedType = MediaType.VIDEO, filter = MediaType.VIDEO, loading = false), onIntent = {}, onBrowse = {})
}
