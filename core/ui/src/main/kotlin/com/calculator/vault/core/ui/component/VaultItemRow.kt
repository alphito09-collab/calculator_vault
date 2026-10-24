package com.calculator.vault.core.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.calculator.vault.core.designsystem.component.SelectionIndicator
import com.calculator.vault.core.designsystem.component.SelectionIndicatorStyle
import com.calculator.vault.core.designsystem.component.cardBorder
import com.calculator.vault.core.designsystem.icon.VaultIcons
import com.calculator.vault.core.designsystem.preview.ThemePreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultRadius
import com.calculator.vault.core.designsystem.theme.VaultTheme
import com.calculator.vault.core.model.VaultItem
import androidx.compose.foundation.border

/** Gradient tile with a waveform/note glyph used for audio items. */
@Composable
fun AudioArt(seed: String, modifier: Modifier = Modifier, size: Dp = 44.dp, @DrawableRes icon: Int = VaultIcons.Waveform) {
    Box(
        modifier.size(size).clip(VaultRadius.Tile).background(placeholderBrush(seed)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(painterResource(icon), null, tint = Color.White, modifier = Modifier.size(size * 0.36f))
    }
}

/** Leading visual for list rows: audio art or the file-type icon (PDF, DOCX, ZIP…). */
@Composable
fun VaultItemLeading(item: VaultItem) {
    if (item.type == com.calculator.vault.core.model.MediaType.AUDIO) {
        AudioArt(item.id, icon = if (item.name.contains("call", ignoreCase = true)) VaultIcons.Phone else VaultIcons.Waveform)
    } else {
        FileTypeIcon(item.name, item.mimeType)
    }
}

/** Card row for audio and files: art, title, "Recorded • 3:58", selection badge. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VaultItemRow(
    item: VaultItem,
    subtitle: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectionMode: Boolean = false,
    selected: Boolean = false,
    playing: Boolean = false,
) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(VaultRadius.Banner)
            .background(if (playing) VaultTheme.scheme.primaryContainer else VaultTheme.scheme.surface)
            .border(cardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(VaultTheme.scheme.outlineVariant)), VaultRadius.Banner)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        VaultItemLeading(item)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (playing) {
                    Icon(painterResource(VaultIcons.Waveform), null, tint = VaultTheme.scheme.primary, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    item.name,
                    style = VaultTheme.text.rowTitle.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium),
                    color = VaultTheme.scheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(subtitle, style = VaultTheme.typography.bodyMedium, color = VaultTheme.scheme.onSurfaceVariant, maxLines = 1)
        }
        if (selectionMode) {
            Spacer(Modifier.width(8.dp))
            SelectionIndicator(selected, size = 28.dp, style = SelectionIndicatorStyle.OnSurface)
        }
    }
}

@ThemePreviews
@Composable
private fun VaultItemRowPreview() = VaultPreview {
    val item = VaultItem("1", com.calculator.vault.core.model.MediaType.AUDIO, "Voice memo", "audio/m4a", 1000, null, null, 0, 0, 238_000)
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        VaultItemRow(item, "Recorded • 3:58", {}, {})
        VaultItemRow(item.copy(id = "2", name = "Meeting Backup"), "Recorded • 3:58", {}, {}, selectionMode = true, selected = true, playing = true)
        VaultItemRow(item.copy(id = "3", type = com.calculator.vault.core.model.MediaType.FILE, name = "Lease agreement.pdf"), "PDF • 2.1 MB", {}, {})
        VaultItemRow(item.copy(id = "4", type = com.calculator.vault.core.model.MediaType.FILE, name = "Budget 2026.xlsx"), "XLSX • 84 KB", {}, {})
        VaultItemRow(item.copy(id = "5", type = com.calculator.vault.core.model.MediaType.FILE, name = "Photos backup.zip"), "ZIP • 1.2 GB", {}, {})
    }
}
