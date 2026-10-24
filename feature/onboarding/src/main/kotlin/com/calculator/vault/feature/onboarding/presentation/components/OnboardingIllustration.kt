package com.calculator.vault.feature.onboarding.presentation.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.calculator.vault.core.designsystem.theme.VaultTheme

/** Full-bleed illustration that fades into the background, with a parallax [offset]. */
@Composable
fun OnboardingIllustration(@DrawableRes image: Int, offset: Float, modifier: Modifier = Modifier, fadeBottom: Boolean = true) {
    val bg = VaultTheme.scheme.background
    Box(modifier.clipToBounds()) {
        Image(
            painter = painterResource(image),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val distance = kotlin.math.abs(offset).coerceAtMost(1f)
                    alpha = 1f - distance * 0.5f
                    scaleX = 1f + distance * 0.08f
                    scaleY = scaleX
                },
        )
        val fade = if (fadeBottom) {
            Brush.verticalGradient(0.62f to Color.Transparent, 1f to bg)
        } else {
            Brush.horizontalGradient(0.7f to Color.Transparent, 1f to bg)
        }
        Box(Modifier.fillMaxSize().background(fade))
        if (VaultTheme.colors.isDark) Box(Modifier.fillMaxSize().background(bg.copy(alpha = 0.18f)))
    }
}
