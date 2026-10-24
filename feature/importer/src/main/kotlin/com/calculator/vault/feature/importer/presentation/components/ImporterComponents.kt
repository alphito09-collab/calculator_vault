package com.calculator.vault.feature.importer.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.calculator.vault.core.designsystem.component.SelectionIndicator
import com.calculator.vault.core.designsystem.component.SelectionIndicatorStyle
import com.calculator.vault.core.designsystem.icon.VaultIcons
import com.calculator.vault.core.designsystem.preview.ThemePreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultRadius
import com.calculator.vault.core.designsystem.theme.VaultTheme
import com.calculator.vault.core.model.DeviceMedia
import com.calculator.vault.core.model.MediaType
import com.calculator.vault.core.ui.component.AudioArt
import com.calculator.vault.core.ui.component.FileTypeIcon
import com.calculator.vault.core.ui.util.formatBytes
import com.calculator.vault.core.ui.util.formatDuration

/** Light-blue "Browse device storage" button that opens the system file picker. */
@Composable
fun BrowseStorageButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(VaultRadius.Card)
            .background(VaultTheme.scheme.primaryContainer.copy(alpha = if (VaultTheme.colors.isDark) 1f else 0.75f))
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painterResource(VaultIcons.FileOutline), null, tint = VaultTheme.scheme.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(text, style = VaultTheme.typography.labelLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium), color = VaultTheme.scheme.primary)
    }
}

/** Selectable list row for device audio and documents. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DeviceMediaRow(item: DeviceMedia, selected: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(VaultRadius.Banner)
            .background(if (selected) VaultTheme.scheme.primaryContainer else VaultTheme.scheme.surface)
            .clickable(onClick = onToggle)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (item.type == MediaType.AUDIO) {
            AudioArt(item.uri)
        } else {
            FileTypeIcon(item.name, item.mimeType)
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(item.name, style = VaultTheme.text.rowTitle, color = VaultTheme.scheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
            val meta = listOfNotNull(item.durationMs?.let(::formatDuration), formatBytes(item.sizeBytes)).joinToString(" • ")
            Text(meta, style = VaultTheme.typography.bodySmall, color = VaultTheme.scheme.onSurfaceVariant)
        }
        SelectionIndicator(selected, size = 26.dp, style = SelectionIndicatorStyle.OnSurface)
    }
}

@ThemePreviews
@Composable
private fun ImporterComponentsPreview() = VaultPreview {
    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        BrowseStorageButton("Browse device storage", {})
        DeviceMediaRow(DeviceMedia("a", MediaType.AUDIO, "Voice memo.m4a", "audio/mp4", 2_400_000, "Recordings", 0, durationMs = 238_000), true, {})
        DeviceMediaRow(DeviceMedia("b", MediaType.FILE, "Tax_2025.pdf", "application/pdf", 610_000, "Download", 0), false, {})
        DeviceMediaRow(DeviceMedia("c", MediaType.FILE, "Pitch deck.pptx", "application/vnd.ms-powerpoint", 3_200_000, "Download", 0), true, {})
    }
}
