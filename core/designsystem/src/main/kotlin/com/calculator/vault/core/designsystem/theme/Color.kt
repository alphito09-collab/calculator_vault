package com.calculator.vault.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Raw palette taken from the Figma file (names follow the file's colour styles). */
internal object Palette {
    // Light ground
    val Pampas = Color(0xFFF7F6F2)
    val White = Color(0xFFFFFFFF)
    val BalticSea = Color(0xFF1C1B22)
    val Dolphin = Color(0xFF6B6975)
    val Charade = Color(0xFF424550)
    val Sand = Color(0xFFE7E3D9)
    val SandBorder = Color(0xFFE4E1D8)
    val Cloud = Color(0xFFEFEDE7)
    val Muted = Color(0xFF747474)
    val SkipGray = Color(0xFF555570)
    val DisabledGray = Color(0xFFB8BCC0)

    // Accent
    val RoyalBlue = Color(0xFF3B94ED)
    val BlueContainer = Color(0xFFE3EFFD)
    val BlueSoft = Color(0xFFE8F2FD)
    val Navy = Color(0xFF23598E)
    val NavyDeep = Color(0xFF001542)
    val Crail = Color(0xFFC1493F)
    val Rose = Color(0xFFC2536B)

    // Category accents
    val Lavender = Color(0xFF7B7BFF)
    val Tangerine = Color(0xFFFF7439)
    val Mint = Color(0xFF34D399)
    val Teal = Color(0xFF3B8091)
    val Plum = Color(0xFF873B91)
    val Indigo = Color(0xFF3B4191)
    val Olive = Color(0xFF6A913B)

    // Dark ground ("nocturne ground, accent as a glow")
    val Night = Color(0xFF0F1117)
    val NightSurface = Color(0xFF181B23)
    val NightSurfaceHigh = Color(0xFF20242E)
    val NightKey = Color(0xFF272B36)
    val NightBorder = Color(0xFF2C303B)
    val NightText = Color(0xFFECEDF2)
    val NightTextSecondary = Color(0xFFA1A3AE)
    val GlowBlue = Color(0xFF5AA7F4)
    val GlowContainer = Color(0xFF16304B)
}

internal val LightColors: ColorScheme = lightColorScheme(
    primary = Palette.RoyalBlue,
    onPrimary = Palette.White,
    primaryContainer = Palette.BlueContainer,
    onPrimaryContainer = Palette.Navy,
    secondary = Palette.Navy,
    onSecondary = Palette.White,
    secondaryContainer = Palette.BlueSoft,
    onSecondaryContainer = Palette.Navy,
    tertiary = Palette.Lavender,
    background = Palette.Pampas,
    onBackground = Palette.BalticSea,
    surface = Palette.White,
    onSurface = Palette.BalticSea,
    surfaceVariant = Palette.Sand,
    onSurfaceVariant = Palette.Dolphin,
    surfaceContainerLowest = Palette.White,
    surfaceContainerLow = Palette.White,
    surfaceContainer = Palette.White,
    surfaceContainerHigh = Palette.White,
    surfaceContainerHighest = Palette.Cloud,
    outline = Palette.SandBorder,
    outlineVariant = Color(0x0F1C1B22),
    error = Palette.Crail,
    onError = Palette.White,
    errorContainer = Color(0xFFFBE7E5),
    onErrorContainer = Palette.Crail,
    inverseSurface = Color(0xFF000000),
    inverseOnSurface = Palette.White,
    scrim = Color(0x59000000),
)

internal val DarkColors: ColorScheme = darkColorScheme(
    primary = Palette.GlowBlue,
    onPrimary = Palette.White,
    primaryContainer = Palette.GlowContainer,
    onPrimaryContainer = Color(0xFFB4D7FB),
    secondary = Color(0xFF8FBDEB),
    onSecondary = Palette.Night,
    secondaryContainer = Palette.GlowContainer,
    onSecondaryContainer = Color(0xFFB4D7FB),
    tertiary = Color(0xFF9F9FFF),
    background = Palette.Night,
    onBackground = Palette.NightText,
    surface = Palette.NightSurface,
    onSurface = Palette.NightText,
    surfaceVariant = Palette.NightKey,
    onSurfaceVariant = Palette.NightTextSecondary,
    surfaceContainerLowest = Palette.Night,
    surfaceContainerLow = Palette.NightSurface,
    surfaceContainer = Palette.NightSurface,
    surfaceContainerHigh = Palette.NightSurfaceHigh,
    surfaceContainerHighest = Palette.NightKey,
    outline = Palette.NightBorder,
    outlineVariant = Color(0x1AFFFFFF),
    error = Color(0xFFF0897F),
    onError = Palette.Night,
    errorContainer = Color(0xFF3A1A18),
    onErrorContainer = Color(0xFFF0897F),
    inverseSurface = Palette.NightText,
    inverseOnSurface = Palette.Night,
    scrim = Color(0x99000000),
)

/** Colours the Material scheme has no slot for. Read them through `VaultTheme.colors`. */
@Immutable
data class VaultColors(
    val keyDigit: Color,
    val keyOperator: Color,
    val onKey: Color,
    val cardBorder: Color,
    val divider: Color,
    val sectionLabel: Color,
    val skip: Color,
    val dialogTitle: Color,
    val disabledContainer: Color,
    val onDisabledContainer: Color,
    val toast: Color,
    val onToast: Color,
    val banner: Color,
    val iconTile: Color,
    val iconTileContent: Color,
    val navIcon: Color,
    val switchTrackOff: Color,
    val danger: Color,
    val photoAccent: Color,
    val videoAccent: Color,
    val audioAccent: Color,
    val fileAccent: Color,
    val lockAccent: Color,
    val themeAccent: Color,
    val backupAccent: Color,
    val intruderAccent: Color,
    /** Soft two-stop gradients used for audio tiles and image placeholders. */
    val tileGradients: List<Pair<Color, Color>>,
    val isDark: Boolean,
)

internal val LightVaultColors = VaultColors(
    keyDigit = Palette.White,
    keyOperator = Palette.Sand,
    onKey = Palette.BalticSea,
    cardBorder = Palette.SandBorder,
    divider = Color(0x0A000000),
    sectionLabel = Palette.Muted,
    skip = Palette.SkipGray,
    dialogTitle = Palette.NavyDeep,
    disabledContainer = Palette.DisabledGray,
    onDisabledContainer = Palette.White,
    toast = Color(0xFF000000),
    onToast = Palette.White,
    banner = Palette.BlueContainer,
    iconTile = Palette.Cloud,
    iconTileContent = Color(0xFFBDBBB6),
    navIcon = Palette.Charade,
    switchTrackOff = Color(0xFFD5D7DC),
    danger = Palette.Rose,
    photoAccent = Palette.Lavender,
    videoAccent = Palette.RoyalBlue,
    audioAccent = Palette.Tangerine,
    fileAccent = Palette.Mint,
    lockAccent = Palette.Teal,
    themeAccent = Palette.Plum,
    backupAccent = Palette.Indigo,
    intruderAccent = Palette.Olive,
    tileGradients = listOf(
        Color(0xFFD9D3F5) to Color(0xFFA99BDB),
        Color(0xFFE8E1D2) to Color(0xFFCBC0A6),
        Color(0xFFD7E1EA) to Color(0xFFA9BFD1),
        Color(0xFFE9D3DC) to Color(0xFFC9A3B5),
        Color(0xFFDDE8D3) to Color(0xFFADC79B),
        Color(0xFFEBD8CB) to Color(0xFFD1A88C),
    ),
    isDark = false,
)

internal val DarkVaultColors = VaultColors(
    keyDigit = Palette.NightSurfaceHigh,
    keyOperator = Palette.NightKey,
    onKey = Palette.NightText,
    cardBorder = Palette.NightBorder,
    divider = Color(0x14FFFFFF),
    sectionLabel = Color(0xFF8D8F99),
    skip = Color(0xFFA6A7BC),
    dialogTitle = Palette.NightText,
    disabledContainer = Color(0xFF3A3E49),
    onDisabledContainer = Color(0xFF8D8F99),
    toast = Color(0xFFECEDF2),
    onToast = Palette.Night,
    banner = Palette.GlowContainer,
    iconTile = Palette.NightSurfaceHigh,
    iconTileContent = Color(0xFF6F7280),
    navIcon = Color(0xFFC3C5CE),
    switchTrackOff = Color(0xFF3A3E49),
    danger = Color(0xFFE47A90),
    photoAccent = Color(0xFF9D9DFF),
    videoAccent = Palette.GlowBlue,
    audioAccent = Color(0xFFFF9466),
    fileAccent = Color(0xFF5BE0B0),
    lockAccent = Color(0xFF6FB6C7),
    themeAccent = Color(0xFFC57FCF),
    backupAccent = Color(0xFF8C93E6),
    intruderAccent = Color(0xFF9CC46A),
    tileGradients = listOf(
        Color(0xFF3A3560) to Color(0xFF2A2647),
        Color(0xFF3F3A2F) to Color(0xFF2D2A22),
        Color(0xFF2E3A47) to Color(0xFF222B35),
        Color(0xFF45313B) to Color(0xFF32242B),
        Color(0xFF34402C) to Color(0xFF262F21),
        Color(0xFF47362C) to Color(0xFF332720),
    ),
    isDark = true,
)

val LocalVaultColors = staticCompositionLocalOf { LightVaultColors }
