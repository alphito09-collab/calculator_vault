package com.calculator.vault.feature.audios.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.calculator.vault.core.designsystem.component.VaultIconButton
import com.calculator.vault.core.designsystem.icon.VaultIcons
import com.calculator.vault.core.designsystem.preview.ThemePreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultRadius
import com.calculator.vault.core.designsystem.theme.VaultTheme
import com.calculator.vault.core.ui.component.AudioArt

/** Dark pill showing what's playing; tap opens the player, the button pauses. */
@Composable
fun MiniPlayer(title: String, subtitle: String, seed: String, playing: Boolean, onClick: () -> Unit, onPlayPause: () -> Unit, modifier: Modifier = Modifier) {
    val bg = if (VaultTheme.colors.isDark) VaultTheme.scheme.surfaceContainerHigh else Color(0xFF3A3A3C)
    Row(
        modifier
            .fillMaxWidth()
            .shadow(12.dp, VaultRadius.Banner)
            .clip(VaultRadius.Banner)
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AudioArt(seed, size = 40.dp)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = VaultTheme.text.rowTitle, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, style = VaultTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f), maxLines = 1)
        }
        VaultIconButton(if (playing) VaultIcons.Pause else VaultIcons.Play, null, onPlayPause, tint = Color.White)
    }
}

/** Big blue round play/pause button. */
@Composable
fun PlayPauseButton(playing: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(72.dp)
            .shadow(12.dp, CircleShape, spotColor = VaultTheme.scheme.primary)
            .clip(CircleShape)
            .background(VaultTheme.scheme.primary)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(painterResource(if (playing) VaultIcons.Pause else VaultIcons.Play), null, tint = Color.White, modifier = Modifier.size(30.dp))
    }
}

/**
 * Vertical band slider for the equalizer: grey track, blue fill from the bottom, round thumb.
 * [value] runs from [min] (bottom) to [max] (top).
 */
@Composable
fun VerticalSlider(value: Float, min: Float, max: Float, onValueChange: (Float) -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val track = VaultTheme.colors.cardBorder
    val active = if (enabled) VaultTheme.scheme.primary else VaultTheme.colors.disabledContainer
    val onChange = rememberUpdatedState(onValueChange)
    fun valueAt(y: Float, height: Float) = (max - (y / height).coerceIn(0f, 1f) * (max - min))
    Canvas(
        modifier
            .width(36.dp)
            .fillMaxHeight()
            .pointerInput(enabled, min, max) {
                if (!enabled) return@pointerInput
                detectTapGestures { onChange.value(valueAt(it.y, size.height.toFloat())) }
            }
            .pointerInput(enabled, min, max) {
                if (!enabled) return@pointerInput
                detectVerticalDragGestures { change, _ ->
                    change.consume()
                    onChange.value(valueAt(change.position.y, size.height.toFloat()))
                }
            },
    ) {
        val x = size.width / 2f
        val pad = 12.dp.toPx()
        val usable = size.height - pad * 2
        val fraction = ((value - min) / (max - min)).coerceIn(0f, 1f)
        val thumbY = pad + usable * (1f - fraction)
        drawLine(track, Offset(x, pad), Offset(x, size.height - pad), 4.dp.toPx(), StrokeCap.Round)
        drawLine(active, Offset(x, thumbY), Offset(x, size.height - pad), 6.dp.toPx(), StrokeCap.Round)
        drawCircle(active, 11.dp.toPx(), Offset(x, thumbY))
        drawCircle(Color.White.copy(alpha = 0.3f), 5.dp.toPx(), Offset(x, thumbY))
    }
}

@ThemePreviews
@Composable
private fun AudioComponentsPreview() = VaultPreview {
    Column(Modifier.padding(20.dp)) {
        MiniPlayer("Meeting Backup", "Recorded • 3:58", "x", true, {}, {})
        Row(Modifier.padding(top = 16.dp)) {
            PlayPauseButton(false, {})
            Box(Modifier.size(width = 40.dp, height = 200.dp)) { VerticalSlider(3f, -15f, 15f, {}) }
        }
    }
}
