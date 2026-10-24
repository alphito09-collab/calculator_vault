package com.calculator.vault.feature.intruder.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.calculator.vault.core.common.VaultConstants
import com.calculator.vault.core.designsystem.component.VaultCard
import com.calculator.vault.core.designsystem.component.WheelPicker
import com.calculator.vault.core.designsystem.icon.VaultIcons
import com.calculator.vault.core.designsystem.preview.ThemePreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultRadius
import com.calculator.vault.core.designsystem.theme.VaultTheme
import com.calculator.vault.core.model.IntruderAttempt
import com.calculator.vault.core.model.IntruderPhotoRequest
import com.calculator.vault.core.ui.util.formatTimestamp
import com.calculator.vault.feature.intruder.R

@Composable
fun StatCard(value: Int, label: String, modifier: Modifier = Modifier) {
    VaultCard(modifier) {
        Column(Modifier.fillMaxWidth().padding(20.dp)) {
            Text(value.toString(), style = VaultTheme.text.stat, color = VaultTheme.scheme.onSurface)
            Spacer(Modifier.height(8.dp))
            Text(label, style = VaultTheme.typography.bodyMedium, color = VaultTheme.scheme.onSurfaceVariant)
        }
    }
}

/** "3 failed attempts · Today, 14:22" with the captured photo (or a camera-off tile) and a badge. */
@Composable
fun AttemptRow(attempt: IntruderAttempt, onClick: () -> Unit, modifier: Modifier = Modifier) {
    VaultCard(onClick = if (attempt.hasPhoto) onClick else null, modifier = modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(56.dp).clip(VaultRadius.Tile).background(VaultTheme.colors.iconTile), contentAlignment = Alignment.Center) {
                if (attempt.hasPhoto) {
                    AsyncImage(IntruderPhotoRequest(attempt.id), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Icon(painterResource(VaultIcons.CameraOff), null, tint = VaultTheme.colors.iconTileContent, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    pluralStringResource(R.plurals.intruder_failed_attempts, attempt.failedAttempts, attempt.failedAttempts),
                    style = VaultTheme.typography.bodyLarge.copy(fontSize = VaultTheme.text.rowTitle.fontSize),
                    color = VaultTheme.scheme.onSurface,
                )
                Text(formatTimestamp(attempt.timestamp), style = VaultTheme.typography.bodyMedium, color = VaultTheme.scheme.onSurfaceVariant)
            }
            Text(
                stringResource(if (attempt.hasPhoto) R.string.intruder_photo else R.string.intruder_no_photo),
                style = VaultTheme.typography.labelSmall,
                color = if (attempt.hasPhoto) VaultTheme.scheme.primary else VaultTheme.scheme.onSurfaceVariant,
                modifier = Modifier
                    .clip(VaultTheme.shapes.extraSmall)
                    .background(if (attempt.hasPhoto) VaultTheme.scheme.primaryContainer else VaultTheme.colors.iconTile)
                    .padding(horizontal = 10.dp, vertical = 5.dp),
            )
        }
    }
}

/** "Wrong PIN limit": a 1–6 wheel inside the intruder settings card. */
@Composable
fun AttemptLimitPicker(limit: Int, enabled: Boolean, onChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(top = 12.dp, bottom = 14.dp)) {
        Column(Modifier.padding(horizontal = 16.dp).alpha(if (enabled) 1f else 0.45f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(stringResource(R.string.intruder_limit_title), style = VaultTheme.text.rowTitle, color = VaultTheme.scheme.onSurface)
            Text(stringResource(R.string.intruder_limit_desc), style = VaultTheme.typography.bodySmall, color = VaultTheme.scheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(10.dp))
        WheelPicker(
            items = AttemptLimits,
            selected = limit,
            onSelectedChange = onChange,
            enabled = enabled,
        )
    }
}

private val AttemptLimits = (VaultConstants.MIN_INTRUDER_ATTEMPTS..VaultConstants.MAX_INTRUDER_ATTEMPTS).toList()

@Composable
fun NoAttemptsCard(modifier: Modifier = Modifier) {
    VaultCard(modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(painterResource(VaultIcons.ShieldCheckOutline), null, tint = VaultTheme.colors.iconTileContent, modifier = Modifier.size(28.dp))
            Text(stringResource(R.string.intruder_none_title), style = VaultTheme.typography.headlineSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium), color = VaultTheme.scheme.onSurface)
            Text(stringResource(R.string.intruder_none_message), style = VaultTheme.typography.bodyMedium, color = VaultTheme.scheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

@ThemePreviews
@Composable
private fun IntruderComponentsPreview() = VaultPreview {
    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(4, "Failed attempts this week", Modifier.weight(1f))
            StatCard(1, "Photos captured on failure", Modifier.weight(1f))
        }
        AttemptRow(IntruderAttempt("1", System.currentTimeMillis(), 3, true), {})
        AttemptRow(IntruderAttempt("2", System.currentTimeMillis() - 86_400_000, 1, false), {})
        NoAttemptsCard()
        VaultCard { AttemptLimitPicker(limit = 3, enabled = true, onChange = {}) }
    }
}
