package com.calculator.vault.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.calculator.vault.core.designsystem.preview.ThemePreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultRadius
import com.calculator.vault.core.designsystem.theme.VaultTheme

/** Rounded 8dp progress track ("28.2 GB of 64 GB"). */
@Composable
fun VaultProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = VaultTheme.scheme.primary,
    track: Color = VaultTheme.colors.iconTile,
) {
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), label = "progress")
    Box(modifier.fillMaxWidth().height(8.dp).clip(VaultRadius.Pill).background(track)) {
        Box(Modifier.fillMaxHeight().fillMaxWidth(animated).clip(VaultRadius.Pill).background(color))
    }
}

/** Centered spinner for whole-screen loading states. */
@Composable
fun LoadingContent(modifier: Modifier = Modifier, message: String? = null) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(Modifier.size(36.dp), color = VaultTheme.scheme.primary, strokeWidth = 3.dp)
            if (message != null) {
                Text(
                    message,
                    style = VaultTheme.typography.bodyMedium,
                    color = VaultTheme.scheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
        }
    }
}

@ThemePreviews
@Composable
private fun ProgressPreview() = VaultPreview {
    Column(Modifier.padding(20.dp)) {
        VaultProgressBar(0.44f)
        Box(Modifier.size(12.dp))
        VaultProgressBar(0.74f, color = VaultTheme.colors.danger)
    }
}
