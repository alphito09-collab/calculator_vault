package com.calculator.vault.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

internal val VaultShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

/** Corner radii used across the design. */
object VaultRadius {
    val Button = RoundedCornerShape(20.dp)
    val Card = RoundedCornerShape(24.dp)
    val Banner = RoundedCornerShape(20.dp)
    val Dialog = RoundedCornerShape(28.dp)
    val Key = RoundedCornerShape(24.dp)
    val Thumbnail = RoundedCornerShape(18.dp)
    val Tile = RoundedCornerShape(14.dp)
    val Pill = RoundedCornerShape(percent = 50)
}
