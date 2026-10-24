package com.calculator.vault.feature.cloudbackup.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.calculator.vault.core.designsystem.component.VaultCard
import com.calculator.vault.core.designsystem.component.VaultProgressBar
import com.calculator.vault.core.designsystem.icon.VaultIcons
import com.calculator.vault.core.designsystem.preview.ThemePreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultTheme
import com.calculator.vault.core.model.BackupFailure
import com.calculator.vault.core.model.BackupStatus
import com.calculator.vault.core.model.StorageQuota
import com.calculator.vault.core.ui.util.formatBytes
import com.calculator.vault.core.ui.util.formatTimestamp
import com.calculator.vault.feature.cloudbackup.R

/** "Backed up · Last backup Today, 09:12" with the Drive storage bar, or the running/failed state. */
@Composable
fun BackupStatusCard(status: BackupStatus, quota: StorageQuota?, modifier: Modifier = Modifier) {
    val interrupted = status is BackupStatus.Interrupted
    val (icon, title, subtitle) = when (status) {
        is BackupStatus.Running -> Triple(VaultIcons.CloudUpload, stringResource(R.string.backup_running), stringResource(R.string.backup_running_percent, (status.progress * 100).toInt()))
        is BackupStatus.Interrupted -> Triple(
            VaultIcons.CloudOff,
            stringResource(R.string.backup_interrupted),
            stringResource(R.string.backup_stopped_at, (status.progress * 100).toInt(), stringResource(status.reason.labelRes())),
        )
        is BackupStatus.Idle -> Triple(
            VaultIcons.CloudCheck,
            stringResource(if (status.lastBackupAt != null) R.string.backup_backed_up else R.string.backup_not_yet),
            status.lastBackupAt?.let { stringResource(R.string.backup_last, formatTimestamp(it)) } ?: stringResource(R.string.backup_tap_to_start),
        )
        BackupStatus.SignedOut -> Triple(VaultIcons.CloudOutline, stringResource(R.string.backup_signed_out_title), stringResource(R.string.backup_signed_out_message))
    }
    VaultCard(modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(icon), null, tint = if (interrupted) VaultTheme.colors.danger else VaultTheme.scheme.secondary, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(title, style = VaultTheme.typography.bodyLarge.copy(fontSize = VaultTheme.text.rowTitle.fontSize), color = VaultTheme.scheme.onSurface)
                    Text(subtitle, style = VaultTheme.typography.bodyMedium, color = VaultTheme.scheme.onSurfaceVariant)
                }
            }
            when {
                status is BackupStatus.Running -> VaultProgressBar(status.progress)
                status is BackupStatus.Interrupted -> VaultProgressBar(status.progress, color = VaultTheme.colors.danger)
                quota != null -> VaultProgressBar(quota.fraction)
            }
            if (quota != null && status !is BackupStatus.SignedOut) {
                Row(Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.backup_used_of, formatBytes(quota.usedBytes), formatBytes(quota.totalBytes)), style = VaultTheme.typography.bodyMedium, color = VaultTheme.scheme.onSurfaceVariant)
                    Spacer(Modifier.weight(1f))
                    Text(stringResource(R.string.backup_free, formatBytes(quota.freeBytes)), style = VaultTheme.typography.bodyMedium, color = VaultTheme.scheme.onSurfaceVariant)
                }
            }
        }
    }
}

private fun BackupFailure.labelRes(): Int = when (this) {
    BackupFailure.NO_CONNECTION -> R.string.backup_reason_connection
    BackupFailure.AUTH_REQUIRED -> R.string.backup_reason_auth
    BackupFailure.QUOTA_EXCEEDED -> R.string.backup_reason_quota
    BackupFailure.UNKNOWN -> R.string.backup_reason_unknown
}

@ThemePreviews
@Composable
private fun BackupStatusPreview() = VaultPreview {
    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        BackupStatusCard(BackupStatus.Idle(System.currentTimeMillis()), StorageQuota(28_200_000_000, 64_000_000_000))
        BackupStatusCard(BackupStatus.Interrupted(0.74f, BackupFailure.NO_CONNECTION), StorageQuota(28_200_000_000, 64_000_000_000))
    }
}
