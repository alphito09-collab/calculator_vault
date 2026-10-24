package com.calculator.vault.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.calculator.vault.core.designsystem.icon.VaultIcons
import com.calculator.vault.core.designsystem.preview.ThemePreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultTheme

/** Centered icon tile + title + subtitle ("No photos yet"). */
@Composable
fun EmptyState(
    @DrawableRes icon: Int,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
) {
    EmptyState(title, message, modifier) {
        IconTile(
            icon = icon,
            size = 72.dp,
            iconSize = 30.dp,
            tint = VaultTheme.colors.iconTileContent,
            background = VaultTheme.colors.iconTile,
        )
    }
}

/** [EmptyState] with a custom illustration, e.g. a file-type icon. */
@Composable
fun EmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
) {
    Column(
        modifier.padding(24.dp).widthIn(max = 320.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        icon()
        Spacer(Modifier.height(32.dp))
        Text(title, style = VaultTheme.typography.headlineSmall, color = VaultTheme.scheme.onBackground, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(message, style = VaultTheme.typography.bodyMedium, color = VaultTheme.scheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

@ThemePreviews
@Composable
private fun EmptyStatePreview() = VaultPreview {
    EmptyState(VaultIcons.Photo, "No photos yet", "Import photos from your gallery to keep them private.")
}
