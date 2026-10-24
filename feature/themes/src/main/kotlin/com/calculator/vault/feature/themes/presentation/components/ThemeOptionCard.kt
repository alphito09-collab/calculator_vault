package com.calculator.vault.feature.themes.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.calculator.vault.core.designsystem.component.SelectionIndicator
import com.calculator.vault.core.designsystem.component.SelectionIndicatorStyle
import com.calculator.vault.core.designsystem.component.VaultCard
import com.calculator.vault.core.designsystem.preview.ThemePreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultTheme
import com.calculator.vault.core.model.ThemeMode

/** Tiny screen mock in the target theme's colours (split for "System default"). */
@Composable
fun ThemeThumbnail(mode: ThemeMode, modifier: Modifier = Modifier) {
    val light = Color(0xFFF7F6F2) to Color(0xFFCBC7BB)
    val dark = Color(0xFF181B23) to Color(0xFF3A3E49)
    Box(modifier.size(width = 60.dp, height = 72.dp).clip(RoundedCornerShape(12.dp))) {
        when (mode) {
            ThemeMode.LIGHT -> MockScreen(light.first, light.second, Modifier.fillMaxWidth())
            ThemeMode.DARK -> MockScreen(dark.first, dark.second, Modifier.fillMaxWidth())
            ThemeMode.SYSTEM -> Row {
                MockScreen(light.first, light.second, Modifier.weight(1f))
                MockScreen(dark.first, dark.second, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MockScreen(bg: Color, line: Color, modifier: Modifier) {
    Column(modifier.fillMaxHeight().background(bg).padding(8.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Box(Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFF1C1B22).copy(alpha = if (bg.red > 0.5f) 0.85f else 0f).compositeOver(bg)))
        Box(Modifier.fillMaxWidth(0.8f).height(4.dp).clip(RoundedCornerShape(2.dp)).background(line))
        Box(Modifier.fillMaxWidth(0.6f).height(4.dp).clip(RoundedCornerShape(2.dp)).background(line))
        Spacer(Modifier.weight(1f))
        Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFF3B94ED)))
    }
}

private fun Color.compositeOver(background: Color): Color {
    val a = alpha
    return Color(red * a + background.red * (1 - a), green * a + background.green * (1 - a), blue * a + background.blue * (1 - a))
}

@Composable
fun ThemeOptionCard(mode: ThemeMode, title: String, subtitle: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val border by animateColorAsState(if (selected) VaultTheme.scheme.primary else Color.Transparent, label = "themeBorder")
    VaultCard(onClick = onClick, border = BorderStroke(2.dp, border), modifier = modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            ThemeThumbnail(mode)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, style = VaultTheme.typography.bodyLarge.copy(fontSize = VaultTheme.text.rowTitle.fontSize), color = VaultTheme.scheme.onSurface)
                Text(subtitle, style = VaultTheme.typography.bodyMedium, color = VaultTheme.scheme.onSurfaceVariant)
            }
            if (selected) SelectionIndicator(true, size = 24.dp, style = SelectionIndicatorStyle.OnSurface)
        }
    }
}

@ThemePreviews
@Composable
private fun ThemeOptionPreview() = VaultPreview {
    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ThemeOptionCard(ThemeMode.SYSTEM, "System default", "Follows your device setting", false, {})
        ThemeOptionCard(ThemeMode.LIGHT, "Light", "Warm off-white and blue accent", true, {})
        ThemeOptionCard(ThemeMode.DARK, "Dark", "Nocturne ground, accent as a glow", false, {})
    }
}
