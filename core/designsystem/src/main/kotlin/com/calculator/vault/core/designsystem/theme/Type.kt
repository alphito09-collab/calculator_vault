package com.calculator.vault.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.calculator.vault.core.designsystem.R

/** Inter is the design's primary face; Roboto (the platform default) is used for app bars and dialogs. */
val InterFamily = FontFamily(
    Font(R.font.inter_light, FontWeight.Light),
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
)

private val Roboto = FontFamily.Default

private fun inter(weight: FontWeight, size: Float, line: Float, tracking: Float = 0f) = TextStyle(
    fontFamily = InterFamily,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = tracking.sp,
)

private fun roboto(weight: FontWeight, size: Float, line: Float, tracking: Float = 0f) = TextStyle(
    fontFamily = Roboto,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = tracking.sp,
)

internal val VaultTypography = Typography(
    // Calculator display
    displayLarge = inter(FontWeight.Light, 56f, 64f, -0.5f),
    displayMedium = inter(FontWeight.Light, 44f, 52f, -0.4f),
    // Onboarding hero
    displaySmall = roboto(FontWeight.Bold, 32f, 37.5f),
    // Section heroes ("Choose one way to secure…")
    headlineLarge = inter(FontWeight.SemiBold, 26f, 31.5f, -0.26f),
    headlineMedium = inter(FontWeight.SemiBold, 22f, 26.6f),
    headlineSmall = inter(FontWeight.SemiBold, 20f, 24.2f),
    // App bar titles
    titleLarge = roboto(FontWeight.Medium, 20f, 23.4f),
    // Card titles
    titleMedium = inter(FontWeight.SemiBold, 16f, 19.4f),
    titleSmall = inter(FontWeight.SemiBold, 13f, 15.7f),
    // Rows and body copy
    bodyLarge = inter(FontWeight.Medium, 14.5f, 17.5f),
    bodyMedium = inter(FontWeight.Normal, 14f, 18f),
    bodySmall = inter(FontWeight.Normal, 12f, 14.5f),
    // Buttons, chips, section labels
    labelLarge = inter(FontWeight.SemiBold, 16f, 24f),
    labelMedium = roboto(FontWeight.Medium, 14f, 16.4f, 0.014f),
    labelSmall = roboto(FontWeight.Medium, 12f, 16f),
)

/** Styles that do not fit Material slots. */
@Immutable
data class VaultTextStyles(
    val calculatorKey: TextStyle,
    val pinKey: TextStyle,
    val dialogTitle: TextStyle,
    val dialogAction: TextStyle,
    val rowTitle: TextStyle,
    val caption: TextStyle,
    val stat: TextStyle,
    val pinMask: TextStyle,
)

internal val DefaultVaultTextStyles = VaultTextStyles(
    calculatorKey = inter(FontWeight.Medium, 22f, 33f),
    pinKey = inter(FontWeight.Normal, 25f, 30f),
    dialogTitle = roboto(FontWeight.SemiBold, 20f, 30f),
    dialogAction = roboto(FontWeight.Medium, 16f, 24f),
    rowTitle = roboto(FontWeight.Normal, 16f, 24f, -0.01f),
    caption = inter(FontWeight.Normal, 11f, 13.3f),
    stat = inter(FontWeight.SemiBold, 32f, 36f, -0.02f),
    pinMask = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Bold, fontSize = 34.sp, letterSpacing = 0.02.em),
)

val LocalVaultTextStyles = staticCompositionLocalOf { DefaultVaultTextStyles }
