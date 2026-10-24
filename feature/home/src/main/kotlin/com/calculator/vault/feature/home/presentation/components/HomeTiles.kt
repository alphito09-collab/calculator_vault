package com.calculator.vault.feature.home.presentation.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.calculator.vault.core.designsystem.component.IconTile
import com.calculator.vault.core.designsystem.component.VaultCard
import com.calculator.vault.core.designsystem.icon.VaultIcons
import com.calculator.vault.core.designsystem.preview.ThemePreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultRadius
import com.calculator.vault.core.designsystem.theme.VaultTheme

/** "App Lock" style tool button: tinted icon + label on a white pill. */
@Composable
fun ToolTile(@DrawableRes icon: Int, label: String, tint: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    VaultCard(onClick = onClick, shape = VaultRadius.Banner, modifier = modifier) {
        Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(icon), null, tint = tint, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(12.dp))
            Text(
                label,
                style = VaultTheme.text.rowTitle.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium),
                color = VaultTheme.scheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Large "Photos · 128 items" card with a tinted icon tile. */
@Composable
fun CategoryCard(@DrawableRes icon: Int, title: String, subtitle: String, accent: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    VaultCard(onClick = onClick, modifier = modifier) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            IconTile(icon = icon, tint = accent, background = accent.copy(alpha = 0.1f), size = 44.dp, iconSize = 22.dp)
            Spacer(Modifier.height(30.dp))
            Text(title, style = VaultTheme.typography.headlineSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium), color = VaultTheme.scheme.onSurface)
            Text(subtitle, style = VaultTheme.typography.bodyMedium, color = VaultTheme.scheme.onSurfaceVariant)
        }
    }
}

@ThemePreviews
@Composable
private fun HomeTilesPreview() = VaultPreview {
    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ToolTile(VaultIcons.Lock, "App Lock", VaultTheme.colors.lockAccent, {}, Modifier.weight(1f))
            ToolTile(VaultIcons.Brush, "Themes", VaultTheme.colors.themeAccent, {}, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CategoryCard(VaultIcons.Photo, "Photos", "128 items", VaultTheme.colors.photoAccent, {}, Modifier.weight(1f))
            CategoryCard(VaultIcons.Music, "Audio", "9 items", VaultTheme.colors.audioAccent, {}, Modifier.weight(1f))
        }
    }
}
