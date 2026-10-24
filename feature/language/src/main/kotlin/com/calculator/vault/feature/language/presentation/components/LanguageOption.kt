package com.calculator.vault.feature.language.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calculator.vault.core.designsystem.component.VaultCard
import com.calculator.vault.core.designsystem.component.VaultRadioButton
import com.calculator.vault.core.designsystem.icon.VaultIcons
import com.calculator.vault.core.designsystem.preview.ThemePreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultTheme
import com.calculator.vault.feature.language.domain.model.AppLanguage

/** One language card: flag/globe, English + native name, radio; blue outline when selected. */
@Composable
fun LanguageOption(language: AppLanguage, selected: Boolean, systemLabel: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val border by animateColorAsState(if (selected) VaultTheme.scheme.primary else Color.Transparent, label = "border")
    VaultCard(onClick = onClick, border = BorderStroke(2.dp, border), modifier = modifier.fillMaxWidth()) {
        Row(Modifier.padding(horizontal = 18.dp, vertical = 18.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(32.dp).clip(CircleShape).background(VaultTheme.colors.iconTile),
                contentAlignment = Alignment.Center,
            ) {
                if (language.tag == null) {
                    Icon(painterResource(VaultIcons.Globe), null, tint = VaultTheme.colors.iconTileContent, modifier = Modifier.size(22.dp))
                } else {
                    Text(language.flag, fontSize = 20.sp)
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    if (language.tag == null) systemLabel else language.englishName,
                    style = VaultTheme.text.rowTitle,
                    color = VaultTheme.scheme.onSurface,
                )
                if (language.tag != null && language.nativeName != language.englishName) {
                    Text(language.nativeName, style = VaultTheme.typography.bodySmall, color = VaultTheme.scheme.onSurfaceVariant)
                }
            }
            VaultRadioButton(selected = selected, onClick = null)
        }
    }
}

@ThemePreviews
@Composable
private fun LanguageOptionPreview() = VaultPreview {
    Column(Modifier.padding(16.dp)) {
        LanguageOption(AppLanguage(null, "System", "System", ""), selected = true, systemLabel = "System", onClick = {})
        Spacer(Modifier.size(10.dp))
        LanguageOption(AppLanguage("de", "German", "Deutsch", "🇩🇪"), selected = false, systemLabel = "System", onClick = {})
    }
}
