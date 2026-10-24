package com.calculator.vault.feature.settings.presentation.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.calculator.vault.core.designsystem.component.IconTile
import com.calculator.vault.core.designsystem.component.VaultBottomSheet
import com.calculator.vault.core.designsystem.component.VaultButton
import com.calculator.vault.core.designsystem.component.VaultTextButton
import com.calculator.vault.core.designsystem.icon.VaultIcons
import com.calculator.vault.core.designsystem.preview.ThemePreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultTheme
import com.calculator.vault.feature.settings.R

/** Explains why "Prevent app uninstall" needs device-admin activation before asking for it. */
@Composable
fun UninstallProtectionSheet(onActivate: () -> Unit, onDismiss: () -> Unit) {
    VaultBottomSheet(onDismissRequest = onDismiss) { UninstallProtectionContent(onActivate, onDismiss) }
}

@Composable
private fun UninstallProtectionContent(onActivate: () -> Unit, onDismiss: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        IconTile(VaultIcons.ShieldAlert, size = 56.dp, iconSize = 28.dp)
        Text(stringResource(R.string.settings_uninstall_sheet_title), style = VaultTheme.text.dialogTitle, color = VaultTheme.colors.dialogTitle)
        Text(stringResource(R.string.settings_uninstall_sheet_message), style = VaultTheme.text.rowTitle, color = VaultTheme.scheme.onSurfaceVariant)
        Point(VaultIcons.PackageX, stringResource(R.string.settings_uninstall_point_block))
        Point(VaultIcons.Settings, stringResource(R.string.settings_uninstall_point_turn_off))
        Point(VaultIcons.ShieldCheck, stringResource(R.string.settings_uninstall_point_privacy))
        Spacer(Modifier.height(4.dp))
        VaultButton(stringResource(R.string.settings_uninstall_activate), onActivate)
        VaultTextButton(stringResource(R.string.settings_not_now), onDismiss, modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}

@Composable
private fun Point(@DrawableRes icon: Int, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(painterResource(icon), null, tint = VaultTheme.scheme.secondary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(text, style = VaultTheme.typography.bodyMedium, color = VaultTheme.scheme.onSurface)
    }
}

private val VaultIcons.Settings get() = VaultIcons.SettingsOutline

@ThemePreviews
@Composable
private fun UninstallSheetPreview() = VaultPreview {
    Column(Modifier.padding(24.dp)) { UninstallProtectionContent({}, {}) }
}
