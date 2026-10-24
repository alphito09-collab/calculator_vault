package com.calculator.vault.core.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.calculator.vault.core.common.VaultConstants
import com.calculator.vault.core.designsystem.component.InfoBanner
import com.calculator.vault.core.designsystem.icon.VaultIcons
import com.calculator.vault.core.designsystem.preview.ThemePreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.ui.R

/** Tells the owner how to reset a forgotten PIN from the calculator ("7860786 ="). */
@Composable
fun ResetCodeBanner(onDismiss: (() -> Unit)?, modifier: Modifier = Modifier) {
    InfoBanner(
        title = stringResource(R.string.ui_reset_code_title),
        message = stringResource(R.string.ui_reset_code_message, VaultConstants.RESET_PIN_CODE),
        icon = VaultIcons.Key,
        onClick = null,
        onDismiss = onDismiss,
        modifier = modifier,
    )
}

@ThemePreviews
@Composable
private fun ResetCodeBannerPreview() = VaultPreview { ResetCodeBanner(onDismiss = {}, modifier = Modifier.padding(16.dp)) }
