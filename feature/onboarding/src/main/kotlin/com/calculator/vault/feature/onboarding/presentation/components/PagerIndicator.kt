package com.calculator.vault.feature.onboarding.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.calculator.vault.core.designsystem.preview.ThemePreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultRadius
import com.calculator.vault.core.designsystem.theme.VaultTheme

/** Blue pill for the current page, grey dots for the rest; animates between pages. */
@Composable
fun PagerIndicator(count: Int, current: Int, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(count) { index ->
            val selected = index == current
            val width by animateDpAsState(if (selected) 24.dp else 6.dp, spring(), label = "indicatorWidth")
            val color by animateColorAsState(
                if (selected) VaultTheme.scheme.primary else VaultTheme.colors.iconTileContent.copy(alpha = 0.6f),
                label = "indicatorColor",
            )
            Box(Modifier.height(6.dp).width(width).clip(VaultRadius.Pill).background(color))
        }
    }
}

@ThemePreviews
@Composable
private fun PagerIndicatorPreview() = VaultPreview { PagerIndicator(3, 0, Modifier.padding(16.dp)) }
