package com.calculator.vault.core.ui.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.calculator.vault.core.designsystem.R as DsR
import com.calculator.vault.core.designsystem.preview.ThemePreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultTheme
import com.calculator.vault.core.model.AppIconCategory
import com.calculator.vault.core.model.AppIconOption
import com.calculator.vault.core.ui.R

@DrawableRes
fun AppIconOption.backgroundRes(): Int = when (this) {
    AppIconOption.DEFAULT -> DsR.drawable.app_icon_default_background
    AppIconOption.CALCULATOR_1 -> DsR.drawable.app_icon_calculator_1_background
    AppIconOption.CALCULATOR_2 -> DsR.drawable.app_icon_calculator_2_background
    AppIconOption.CALCULATOR_3 -> DsR.drawable.app_icon_calculator_3_background
    AppIconOption.CALCULATOR_4 -> DsR.drawable.app_icon_calculator_4_background
    AppIconOption.MUSIC_1 -> DsR.drawable.app_icon_music_1_background
    AppIconOption.MUSIC_2 -> DsR.drawable.app_icon_music_2_background
    AppIconOption.MUSIC_3 -> DsR.drawable.app_icon_music_3_background
    AppIconOption.MUSIC_4 -> DsR.drawable.app_icon_music_4_background
    AppIconOption.NOTES_1 -> DsR.drawable.app_icon_notes_1_background
    AppIconOption.NOTES_2 -> DsR.drawable.app_icon_notes_2_background
    AppIconOption.NOTES_3 -> DsR.drawable.app_icon_notes_3_background
    AppIconOption.NOTES_4 -> DsR.drawable.app_icon_notes_4_background
    AppIconOption.MAPS_1 -> DsR.drawable.app_icon_maps_1_background
    AppIconOption.MAPS_2 -> DsR.drawable.app_icon_maps_2_background
    AppIconOption.MAPS_3 -> DsR.drawable.app_icon_maps_3_background
    AppIconOption.MAPS_4 -> DsR.drawable.app_icon_maps_4_background
}

@DrawableRes
fun AppIconOption.foregroundRes(): Int = when (this) {
    AppIconOption.DEFAULT -> DsR.drawable.app_icon_default_foreground
    AppIconOption.CALCULATOR_1 -> DsR.drawable.app_icon_calculator_1_foreground
    AppIconOption.CALCULATOR_2 -> DsR.drawable.app_icon_calculator_2_foreground
    AppIconOption.CALCULATOR_3 -> DsR.drawable.app_icon_calculator_3_foreground
    AppIconOption.CALCULATOR_4 -> DsR.drawable.app_icon_calculator_4_foreground
    AppIconOption.MUSIC_1 -> DsR.drawable.app_icon_music_1_foreground
    AppIconOption.MUSIC_2 -> DsR.drawable.app_icon_music_2_foreground
    AppIconOption.MUSIC_3 -> DsR.drawable.app_icon_music_3_foreground
    AppIconOption.MUSIC_4 -> DsR.drawable.app_icon_music_4_foreground
    AppIconOption.NOTES_1 -> DsR.drawable.app_icon_notes_1_foreground
    AppIconOption.NOTES_2 -> DsR.drawable.app_icon_notes_2_foreground
    AppIconOption.NOTES_3 -> DsR.drawable.app_icon_notes_3_foreground
    AppIconOption.NOTES_4 -> DsR.drawable.app_icon_notes_4_foreground
    AppIconOption.MAPS_1 -> DsR.drawable.app_icon_maps_1_foreground
    AppIconOption.MAPS_2 -> DsR.drawable.app_icon_maps_2_foreground
    AppIconOption.MAPS_3 -> DsR.drawable.app_icon_maps_3_foreground
    AppIconOption.MAPS_4 -> DsR.drawable.app_icon_maps_4_foreground
}

/** Launcher label shown under the icon (and as the activity-alias label). */
@StringRes
fun AppIconOption.labelRes(): Int = when (category) {
    AppIconCategory.DEFAULT -> R.string.ui_icon_label_default
    AppIconCategory.CALCULATOR -> R.string.ui_icon_label_calculator
    AppIconCategory.MUSIC -> R.string.ui_icon_label_music
    AppIconCategory.NOTES -> R.string.ui_icon_label_notes
    AppIconCategory.MAPS -> R.string.ui_icon_label_maps
}

@StringRes
fun AppIconCategory.titleRes(): Int = when (this) {
    AppIconCategory.DEFAULT -> R.string.ui_icon_category_default
    AppIconCategory.CALCULATOR -> R.string.ui_icon_category_calculator
    AppIconCategory.MUSIC -> R.string.ui_icon_category_music
    AppIconCategory.NOTES -> R.string.ui_icon_category_notes
    AppIconCategory.MAPS -> R.string.ui_icon_category_maps
}

/**
 * Renders an adaptive launcher icon from its two layers, matching what the launcher shows.
 * The 108dp layers are drawn at 1.5× and clipped to the visible 72dp area.
 */
@Composable
fun AppIconArt(icon: AppIconOption, modifier: Modifier = Modifier, size: Dp = 64.dp, elevated: Boolean = true) {
    val shape = RoundedCornerShape(size * 0.28f)
    Box(
        modifier
            .size(size)
            .then(if (elevated) Modifier.shadow(size * 0.12f, shape) else Modifier)
            .clip(shape)
            .border(0.5.dp, VaultTheme.colors.cardBorder, shape),
        contentAlignment = Alignment.Center,
    ) {
        Image(painterResource(icon.backgroundRes()), null, Modifier.fillMaxSize())
        Image(painterResource(icon.foregroundRes()), null, Modifier.requiredSize(size * 1.5f))
    }
}

@ThemePreviews
@Composable
private fun AppIconArtPreview() = VaultPreview {
    Row(Modifier.padding(16.dp)) {
        listOf(AppIconOption.DEFAULT, AppIconOption.MUSIC_1, AppIconOption.NOTES_1, AppIconOption.MAPS_2).forEach {
            AppIconArt(it, Modifier.padding(6.dp))
        }
    }
}
