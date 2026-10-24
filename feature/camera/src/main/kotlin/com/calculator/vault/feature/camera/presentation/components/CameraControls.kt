package com.calculator.vault.feature.camera.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.calculator.vault.core.designsystem.component.rememberPressScale
import com.calculator.vault.core.designsystem.component.scaleOnPress
import com.calculator.vault.core.designsystem.preview.ThemePreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultRadius
import com.calculator.vault.core.designsystem.theme.VaultTheme
import com.calculator.vault.feature.camera.presentation.mvi.CaptureMode

/** White ring shutter; red dot for video, morphing to a rounded square while recording. */
@Composable
fun ShutterButton(mode: CaptureMode, recording: Boolean, onClick: () -> Unit, contentDescription: String) {
    val interaction = remember { MutableInteractionSource() }
    val scale = rememberPressScale(interaction, 0.9f)
    val innerColor by animateColorAsState(if (mode == CaptureMode.VIDEO) Color(0xFFE53935) else Color.White, label = "shutterColor")
    val innerSize by animateDpAsState(if (recording) 28.dp else 60.dp, label = "shutterSize")
    val corner by animateDpAsState(if (recording) 8.dp else 30.dp, label = "shutterCorner")
    Box(
        Modifier
            .size(78.dp)
            .scaleOnPress(scale)
            .clip(CircleShape)
            .border(4.dp, Color.White, CircleShape)
            .clickable(interaction, indication = null, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.size(innerSize).clip(RoundedCornerShape(corner)).background(innerColor))
    }
}

@Composable
fun ModeSwitch(mode: CaptureMode, photoLabel: String, videoLabel: String, onSelect: (CaptureMode) -> Unit, enabled: Boolean) {
    Row(
        Modifier.clip(VaultRadius.Pill).background(Color.Black.copy(alpha = 0.45f)).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        listOf(CaptureMode.PHOTO to photoLabel, CaptureMode.VIDEO to videoLabel).forEach { (m, label) ->
            val selected = m == mode
            Text(
                label,
                style = VaultTheme.typography.labelMedium,
                color = if (selected) Color.Black else Color.White,
                modifier = Modifier
                    .clip(VaultRadius.Pill)
                    .background(if (selected) Color.White else Color.Transparent)
                    .clickable(enabled = enabled) { onSelect(m) }
                    .padding(horizontal = 18.dp, vertical = 8.dp),
            )
        }
    }
}

@ThemePreviews
@Composable
private fun CameraControlsPreview() = VaultPreview {
    Row(Modifier.background(Color.DarkGray).padding(20.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        ShutterButton(CaptureMode.PHOTO, false, {}, "Shutter")
        ShutterButton(CaptureMode.VIDEO, true, {}, "Shutter")
        ModeSwitch(CaptureMode.PHOTO, "Photo", "Video", {}, true)
    }
}
