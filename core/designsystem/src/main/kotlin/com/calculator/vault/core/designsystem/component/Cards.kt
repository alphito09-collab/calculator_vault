package com.calculator.vault.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.calculator.vault.core.designsystem.preview.ThemePreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultRadius
import com.calculator.vault.core.designsystem.theme.VaultTheme

/**
 * White rounded surface used for list groups, tiles and option cards. Clickable cards get a
 * bounded ripple plus a subtle press scale.
 */
@Composable
fun VaultCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = VaultRadius.Card,
    color: Color = VaultTheme.scheme.surface,
    border: BorderStroke? = null,
    enabled: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val scale = rememberPressScale(interaction, pressedScale = 0.98f)
    var base = modifier
    if (onClick != null) base = base.scaleOnPress(scale)
    base = base.clip(shape).background(color)
    if (border != null) base = base.border(border, shape)
    if (onClick != null) {
        base = base.clickable(interaction, ripple(), enabled = enabled, role = Role.Button, onClick = onClick)
    }
    CompositionLocalProvider(LocalContentColor provides VaultTheme.scheme.onSurface) {
        Box(base, content = content)
    }
}

/** A [VaultCard] laid out as a column, for grouped rows. */
@Composable
fun VaultCardColumn(
    modifier: Modifier = Modifier,
    shape: Shape = VaultRadius.Card,
    content: @Composable ColumnScope.() -> Unit,
) {
    VaultCard(modifier = modifier, shape = shape) { Column(content = content) }
}

@Composable
fun cardBorder(): BorderStroke = BorderStroke(1.dp, VaultTheme.colors.cardBorder)

@ThemePreviews
@Composable
private fun CardsPreview() = VaultPreview {
    Column(Modifier.padding(20.dp)) {
        VaultCard(onClick = {}, border = cardBorder()) {
            Text("Clickable card with border", Modifier.padding(20.dp))
        }
        Box(Modifier.padding(6.dp))
        VaultCard { Text("Plain card", Modifier.padding(20.dp)) }
    }
}
