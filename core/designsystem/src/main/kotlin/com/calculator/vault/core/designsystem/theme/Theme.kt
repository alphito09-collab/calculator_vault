package com.calculator.vault.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.material3.Shapes
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember

/**
 * App theme. Dynamic colour is intentionally off: the disguise depends on the vault looking
 * the same on every device.
 */
@Composable
fun CalculatorVaultTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val vaultColors = if (darkTheme) DarkVaultColors else LightVaultColors
    val ripple = remember(darkTheme) {
        RippleConfiguration(
            color = colorScheme.primary,
            rippleAlpha = if (darkTheme) DarkRippleAlpha else LightRippleAlpha,
        )
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = VaultTypography,
        shapes = VaultShapes,
    ) {
        CompositionLocalProvider(
            LocalVaultColors provides vaultColors,
            LocalVaultTextStyles provides DefaultVaultTextStyles,
            LocalRippleConfiguration provides ripple,
            content = content,
        )
    }
}

private val LightRippleAlpha = RippleAlpha(
    pressedAlpha = 0.14f,
    focusedAlpha = 0.12f,
    draggedAlpha = 0.10f,
    hoveredAlpha = 0.06f,
)

private val DarkRippleAlpha = RippleAlpha(
    pressedAlpha = 0.20f,
    focusedAlpha = 0.16f,
    draggedAlpha = 0.14f,
    hoveredAlpha = 0.08f,
)

/** Shorthand accessors: `VaultTheme.colors`, `VaultTheme.text`, plus the Material ones. */
object VaultTheme {
    val colors: VaultColors
        @Composable @ReadOnlyComposable get() = LocalVaultColors.current
    val text: VaultTextStyles
        @Composable @ReadOnlyComposable get() = LocalVaultTextStyles.current
    val scheme: ColorScheme
        @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme
    val typography: Typography
        @Composable @ReadOnlyComposable get() = MaterialTheme.typography
    val shapes: Shapes
        @Composable @ReadOnlyComposable get() = MaterialTheme.shapes
}
