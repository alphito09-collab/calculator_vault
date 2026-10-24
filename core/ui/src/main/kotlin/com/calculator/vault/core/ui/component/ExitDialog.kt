package com.calculator.vault.core.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.calculator.vault.core.designsystem.component.VaultDialog
import com.calculator.vault.core.ui.R

/** Shown when pressing back on a root screen (calculator, vault home). */
@Composable
fun ExitAppDialog(onExit: () -> Unit, onDismiss: () -> Unit) {
    VaultDialog(
        title = stringResource(R.string.ui_exit_title),
        message = stringResource(R.string.ui_exit_message),
        confirmText = stringResource(R.string.ui_exit),
        onConfirm = onExit,
        onDismissRequest = onDismiss,
    )
}
