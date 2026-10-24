package com.calculator.vault.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.calculator.vault.core.designsystem.component.UnderlinedTextField
import com.calculator.vault.core.designsystem.component.VaultDialog
import com.calculator.vault.core.designsystem.component.VaultDialogBody
import com.calculator.vault.core.designsystem.component.VaultDialogContainer
import com.calculator.vault.core.designsystem.component.VaultProgressBar
import com.calculator.vault.core.designsystem.component.VaultTextButton
import com.calculator.vault.core.designsystem.preview.ThemePreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultRadius
import com.calculator.vault.core.designsystem.theme.VaultTheme
import com.calculator.vault.core.model.MediaType
import com.calculator.vault.core.model.TransferProgress
import com.calculator.vault.core.model.VaultItem
import com.calculator.vault.core.model.VaultThumbnailRequest
import com.calculator.vault.core.ui.R
import com.calculator.vault.core.ui.util.formatBytes
import com.calculator.vault.core.ui.util.formatTimestamp
import com.calculator.vault.core.ui.util.typeLabel

enum class BulkAction { Delete, Unhide, Hide, Restore, DeleteForever }

/** "Delete photos / Are you sure you want to delete 2 photos from your device." */
@Composable
fun ConfirmBulkActionDialog(
    action: BulkAction,
    type: MediaType?,
    count: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val label = type?.let { typeLabel(it, count) } ?: stringResource(R.string.ui_type_items)
    val (title, message, confirm) = when (action) {
        BulkAction.Delete -> Triple(
            stringResource(R.string.ui_delete_title, label),
            stringResource(R.string.ui_delete_message, count, label),
            stringResource(R.string.ui_delete),
        )
        BulkAction.Unhide -> Triple(
            stringResource(R.string.ui_unhide_title, label),
            stringResource(R.string.ui_unhide_message, count, label),
            stringResource(R.string.ui_unhide),
        )
        BulkAction.Hide -> Triple(
            stringResource(R.string.ui_hide_title, label),
            stringResource(R.string.ui_hide_message, count, label),
            stringResource(R.string.ui_hide),
        )
        BulkAction.Restore -> Triple(
            stringResource(R.string.ui_restore_title),
            stringResource(R.string.ui_restore_message, count),
            stringResource(R.string.ui_restore),
        )
        BulkAction.DeleteForever -> Triple(
            stringResource(R.string.ui_delete_forever_title),
            stringResource(R.string.ui_delete_forever_message, count),
            stringResource(R.string.ui_delete),
        )
    }
    VaultDialog(
        title = title.replaceFirstChar { it.uppercase() },
        message = message,
        confirmText = confirm,
        onConfirm = onConfirm,
        onDismissRequest = onDismiss,
        destructive = action == BulkAction.DeleteForever,
    )
}

/** Non-dismissable progress while files are encrypted or restored. */
@Composable
fun TransferProgressDialog(title: String, progress: TransferProgress) {
    VaultDialogContainer(onDismissRequest = {}, dismissOnOutsideClick = false) {
        Text(title, style = VaultTheme.text.dialogTitle, color = VaultTheme.colors.dialogTitle)
        Spacer(Modifier.height(16.dp))
        Text(
            stringResource(R.string.ui_transfer_progress, (progress.completed + progress.failed).coerceAtMost(progress.total), progress.total),
            style = VaultTheme.typography.bodyMedium,
            color = VaultTheme.scheme.onSurfaceVariant,
        )
        progress.currentName?.let {
            Text(it, style = VaultTheme.typography.bodySmall, color = VaultTheme.scheme.onSurfaceVariant, maxLines = 1)
        }
        Spacer(Modifier.height(16.dp))
        VaultProgressBar(progress.fraction)
    }
}

@Composable
fun RenameDialog(currentName: String, onRename: (String) -> Unit, onDismiss: () -> Unit) {
    var value by rememberSaveable { mutableStateOf(currentName.substringBeforeLast('.', currentName)) }
    VaultDialog(
        title = stringResource(R.string.ui_rename_title),
        confirmText = stringResource(R.string.ui_rename),
        onConfirm = { onRename(value) },
        onDismissRequest = onDismiss,
        confirmEnabled = value.isNotBlank(),
    ) {
        UnderlinedTextField(value, { value = it }, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done))
    }
}

/** Item details: thumbnail header + key/value rows. */
@Composable
fun DetailsDialog(item: VaultItem, currentPath: String?, onDismiss: () -> Unit) {
    VaultDialogContainer(onDismissRequest = onDismiss) {
        Row(
            Modifier.fillMaxWidth().clip(VaultRadius.Banner).background(VaultTheme.scheme.primaryContainer).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(60.dp).clip(VaultRadius.Tile).background(placeholderBrush(item.id))) {
                if (item.hasThumbnail) AsyncImage(VaultThumbnailRequest(item.id), null, Modifier.size(60.dp), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(item.name, style = VaultTheme.typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium), color = VaultTheme.scheme.onSurface, maxLines = 2)
                Text(formatBytes(item.sizeBytes), style = VaultTheme.typography.bodySmall, color = VaultTheme.scheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(12.dp))
        DetailRow(stringResource(R.string.ui_detail_original_path), item.originalPath ?: "—")
        DetailRow(stringResource(R.string.ui_detail_current_path), currentPath ?: stringResource(R.string.ui_detail_in_vault))
        if (item.width != null && item.height != null) DetailRow(stringResource(R.string.ui_detail_resolution), "${item.width}×${item.height}")
        DetailRow(stringResource(R.string.ui_detail_created), formatTimestamp(item.dateTaken))
        DetailRow(stringResource(R.string.ui_detail_hidden), formatTimestamp(item.dateAdded))
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            VaultTextButton(stringResource(R.string.ui_close), onDismiss)
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Text(label, style = VaultTheme.text.rowTitle, color = VaultTheme.scheme.onSurfaceVariant, modifier = Modifier.weight(0.42f))
        Text(value, style = VaultTheme.typography.bodyMedium, color = VaultTheme.scheme.onSurface, modifier = Modifier.weight(0.58f))
    }
}

/** "Manage files permission" — explains All-files access before opening system settings. */
@Composable
fun FilesPermissionDialog(onAllow: () -> Unit, onDeny: () -> Unit) {
    VaultDialog(
        title = stringResource(R.string.ui_files_permission_title),
        message = stringResource(R.string.ui_files_permission_message),
        confirmText = stringResource(R.string.ui_allow_access),
        onConfirm = onAllow,
        dismissText = stringResource(R.string.ui_deny),
        onDismissRequest = onDeny,
    )
}

@ThemePreviews
@Composable
private fun DialogsPreview() = VaultPreview {
    Column(Modifier.padding(16.dp).clip(VaultRadius.Dialog).background(VaultTheme.scheme.surface).padding(20.dp)) {
        VaultDialogBody(
            title = stringResource(R.string.ui_files_permission_title),
            message = stringResource(R.string.ui_files_permission_message),
            confirmText = stringResource(R.string.ui_allow_access),
            onConfirm = {},
            dismissText = stringResource(R.string.ui_deny),
        )
    }
}
