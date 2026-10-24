package com.calculator.vault.feature.home.presentation.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.calculator.vault.core.designsystem.component.VaultDialog
import com.calculator.vault.core.designsystem.component.UnderlinedTextField
import com.calculator.vault.core.domain.repository.StorageRepository
import com.calculator.vault.core.domain.usecase.PinRules
import com.calculator.vault.core.ui.util.formatTimestamp
import com.calculator.vault.feature.home.R

/** Found an encrypted vault from a previous install: unlock it with its old PIN. */
@Composable
fun RestoreVaultDialog(
    previous: StorageRepository.PreviousVault,
    pin: String,
    error: Boolean,
    restoring: Boolean,
    onPinChange: (String) -> Unit,
    onRestore: () -> Unit,
    onStartFresh: () -> Unit,
) {
    VaultDialog(
        title = stringResource(R.string.home_restore_title),
        message = stringResource(R.string.home_restore_message, formatTimestamp(previous.createdAt), previous.itemCount),
        confirmText = stringResource(R.string.home_restore),
        onConfirm = onRestore,
        confirmEnabled = PinRules.isValid(pin) && !restoring,
        dismissText = stringResource(R.string.home_start_fresh),
        onDismiss = onStartFresh,
        onDismissRequest = {},
        dismissOnOutsideClick = false,
    ) {
        UnderlinedTextField(
            value = pin,
            onValueChange = onPinChange,
            placeholder = stringResource(R.string.home_restore_pin_hint),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            visualTransformation = PasswordVisualTransformation(),
            isError = error,
            errorText = stringResource(R.string.home_restore_wrong_pin),
        )
    }
}
