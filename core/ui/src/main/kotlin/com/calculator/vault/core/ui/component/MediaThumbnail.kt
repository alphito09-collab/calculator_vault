package com.calculator.vault.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.calculator.vault.core.designsystem.component.SelectionIndicator
import com.calculator.vault.core.designsystem.icon.VaultIcons
import com.calculator.vault.core.designsystem.preview.ThemePreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultRadius
import com.calculator.vault.core.designsystem.theme.VaultTheme
import com.calculator.vault.core.model.VaultThumbnailRequest
import com.calculator.vault.core.ui.util.formatDuration
import kotlin.math.absoluteValue

/** Soft gradient used while a thumbnail decrypts (and for items without one). */
@Composable
fun placeholderBrush(seed: String): Brush {
    val gradients = VaultTheme.colors.tileGradients
    val (start, end) = gradients[seed.hashCode().absoluteValue % gradients.size]
    return remember(start, end) { Brush.linearGradient(listOf(start, end)) }
}

/**
 * Square vault thumbnail: decrypted preview, optional video badge and selection ring.
 * [model] defaults to the encrypted thumbnail of [itemId]; pass a device Uri for the importer.
 */
@Composable
fun MediaThumbnail(
    itemId: String,
    modifier: Modifier = Modifier,
    model: Any? = VaultThumbnailRequest(itemId),
    durationMs: Long? = null,
    selectionMode: Boolean = false,
    selected: Boolean = false,
) {
    Box(
        modifier
            .aspectRatio(1f)
            .clip(VaultRadius.Thumbnail)
            .background(placeholderBrush(itemId))
            .then(if (selected) Modifier.border(2.dp, VaultTheme.scheme.primary, VaultRadius.Thumbnail) else Modifier),
    ) {
        if (model != null) {
            AsyncImage(
                model = model,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        if (durationMs != null) {
            Box(
                Modifier.align(Alignment.Center).size(26.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painterResource(VaultIcons.Play), null, tint = Color.White, modifier = Modifier.size(12.dp))
            }
            Text(
                formatDuration(durationMs),
                style = VaultTheme.text.caption,
                color = Color.White,
                modifier = Modifier.align(Alignment.BottomStart).padding(10.dp),
            )
        }
        if (selectionMode) {
            SelectionIndicator(selected, Modifier.align(Alignment.TopEnd).padding(8.dp))
        }
    }
}

@ThemePreviews
@Composable
private fun MediaThumbnailPreview() = VaultPreview {
    Box(Modifier.padding(16.dp).size(120.dp)) {
        MediaThumbnail("a", model = null, durationMs = 42_000, selectionMode = true, selected = true)
    }
}
