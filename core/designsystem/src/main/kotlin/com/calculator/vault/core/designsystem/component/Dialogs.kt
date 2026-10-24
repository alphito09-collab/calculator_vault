package com.calculator.vault.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.calculator.vault.core.designsystem.R
import com.calculator.vault.core.designsystem.preview.ThemePreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultRadius
import com.calculator.vault.core.designsystem.theme.VaultTheme

/**
 * Confirmation dialog in the design's style: 28dp radius, navy title, grey body and
 * right-aligned text actions ("Cancel  Delete").
 */
@Composable
fun VaultDialog(
    title: String,
    onDismissRequest: () -> Unit,
    confirmText: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    message: String? = null,
    dismissText: String? = stringResource(R.string.ds_cancel),
    onDismiss: () -> Unit = onDismissRequest,
    destructive: Boolean = false,
    confirmEnabled: Boolean = true,
    dismissOnOutsideClick: Boolean = true,
    content: (@Composable ColumnScope.() -> Unit)? = null,
) {
    VaultDialogContainer(onDismissRequest, modifier, dismissOnOutsideClick) {
        VaultDialogBody(
            title = title,
            message = message,
            confirmText = confirmText,
            onConfirm = onConfirm,
            dismissText = dismissText,
            onDismiss = onDismiss,
            destructive = destructive,
            confirmEnabled = confirmEnabled,
            content = content,
        )
    }
}

/** Dialog chrome without opinions about the content. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultDialogContainer(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    dismissOnOutsideClick: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier.widthIn(max = 420.dp),
        properties = DialogProperties(dismissOnClickOutside = dismissOnOutsideClick, usePlatformDefaultWidth = false),
    ) {
        Column(
            Modifier
                .padding(horizontal = 16.dp)
                .clip(VaultRadius.Dialog)
                .background(VaultTheme.scheme.surface)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            content = content,
        )
    }
}

/** The inner layout of [VaultDialog]; exposed so previews can render it without a window. */
@Composable
fun VaultDialogBody(
    title: String,
    confirmText: String,
    onConfirm: () -> Unit,
    message: String? = null,
    dismissText: String? = null,
    onDismiss: () -> Unit = {},
    destructive: Boolean = false,
    confirmEnabled: Boolean = true,
    content: (@Composable ColumnScope.() -> Unit)? = null,
) {
    CompositionLocalProvider(LocalContentColor provides VaultTheme.scheme.onSurface) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(title, style = VaultTheme.text.dialogTitle, color = VaultTheme.colors.dialogTitle)
            if (message != null) {
                Text(message, style = VaultTheme.text.rowTitle, color = VaultTheme.scheme.onSurfaceVariant)
            }
            content?.invoke(this)
        }
        Spacer(Modifier.height(28.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
            if (dismissText != null) VaultTextButton(dismissText, onDismiss)
            VaultTextButton(
                confirmText,
                onConfirm,
                enabled = confirmEnabled,
                color = if (destructive) VaultTheme.scheme.error else VaultTheme.scheme.primary,
            )
        }
    }
}

/** Rounded bottom sheet with a drag handle, used for permission explanations and pickers. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = VaultTheme.scheme.surface,
        dragHandle = {
            Box(
                Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(VaultRadius.Pill)
                    .background(VaultTheme.colors.cardBorder),
            )
        },
    ) {
        Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 24.dp).navigationBarsPadding(), content = content)
    }
}

@ThemePreviews
@Composable
private fun DialogPreview() = VaultPreview {
    Box(Modifier.background(Color.Black.copy(alpha = 0.35f)).padding(16.dp)) {
        Column(
            Modifier.clip(VaultRadius.Dialog).background(VaultTheme.scheme.surface).padding(horizontal = 20.dp, vertical = 24.dp),
        ) {
            VaultDialogBody(
                title = "Delete photos",
                message = "Are you sure you want to delete 2 photos from your device.",
                confirmText = "Delete",
                onConfirm = {},
                dismissText = "Cancel",
            )
        }
    }
}
