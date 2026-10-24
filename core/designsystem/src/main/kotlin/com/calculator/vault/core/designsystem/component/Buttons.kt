package com.calculator.vault.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.calculator.vault.core.designsystem.icon.VaultIcons
import com.calculator.vault.core.designsystem.preview.ThemePreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.Spacing
import com.calculator.vault.core.designsystem.theme.VaultRadius
import com.calculator.vault.core.designsystem.theme.VaultTheme

enum class VaultButtonStyle { Primary, Danger, Tonal }

/** The 64dp, 20dp-radius call-to-action button used at the bottom of most screens. */
@Composable
fun VaultButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    style: VaultButtonStyle = VaultButtonStyle.Primary,
    @DrawableRes leadingIcon: Int? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    val scale = rememberPressScale(interaction, pressedScale = 0.98f)
    val active = enabled && !loading
    val container by animateColorAsState(
        targetValue = when {
            !enabled -> VaultTheme.colors.disabledContainer
            style == VaultButtonStyle.Danger -> VaultTheme.colors.danger
            style == VaultButtonStyle.Tonal -> VaultTheme.scheme.primaryContainer
            else -> VaultTheme.scheme.primary
        },
        label = "buttonContainer",
    )
    val content = when {
        !enabled -> VaultTheme.colors.onDisabledContainer
        style == VaultButtonStyle.Tonal -> VaultTheme.scheme.primary
        else -> Color.White
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(Spacing.buttonHeight)
            .scaleOnPress(scale)
            .clip(VaultRadius.Button)
            .background(container)
            .clickable(
                interactionSource = interaction,
                indication = ripple(color = Color.White),
                enabled = active,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = loading,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "buttonLoading",
        ) { isLoading ->
            if (isLoading) {
                CircularProgressIndicator(Modifier.size(24.dp), color = content, strokeWidth = 2.5.dp)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (leadingIcon != null) {
                        Icon(painterResource(leadingIcon), null, tint = content, modifier = Modifier.size(20.dp))
                        Box(Modifier.width(Spacing.sm))
                    }
                    Text(text, style = VaultTheme.typography.labelLarge, color = content, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

/** Text-only action, as used in dialogs ("Cancel", "Delete") and app bars ("Select all"). */
@Composable
fun VaultTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = VaultTheme.scheme.primary,
    small: Boolean = false,
) {
    Text(
        text = text,
        style = if (small) VaultTheme.typography.labelSmall.copy(fontWeight = null) else VaultTheme.text.dialogAction,
        color = if (enabled) color else color.copy(alpha = 0.4f),
        modifier = modifier
            .clip(VaultRadius.Pill)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
    )
}

/** Small outlined pill ("Reset Password"). */
@Composable
fun VaultOutlinedChipButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = VaultTheme.typography.labelSmall,
        color = VaultTheme.scheme.primary,
        modifier = modifier
            .clip(VaultTheme.shapes.small)
            .border(BorderStroke(1.dp, VaultTheme.scheme.primary), VaultTheme.shapes.small)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp),
    )
}

/** 24dp icon inside a 44dp circular touch target with a bounded ripple. */
@Composable
fun VaultIconButton(
    @DrawableRes icon: Int,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = LocalContentColor.current,
    iconSize: Dp = 24.dp,
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = contentDescription,
            tint = if (enabled) tint else tint.copy(alpha = 0.38f),
            modifier = Modifier.size(iconSize),
        )
    }
}

/** Round floating "+" button with a soft shadow. */
@Composable
fun VaultFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    @DrawableRes icon: Int = VaultIcons.Plus,
    size: Dp = 64.dp,
    filled: Boolean = false,
) {
    val interaction = remember { MutableInteractionSource() }
    val scale = rememberPressScale(interaction, pressedScale = 0.92f)
    val container = if (filled) VaultTheme.scheme.primary else VaultTheme.scheme.surface
    val tint = if (filled) Color.White else VaultTheme.scheme.primary
    Box(
        modifier = modifier
            .size(size)
            .scaleOnPress(scale)
            .shadow(12.dp, if (filled) VaultRadius.Button else CircleShape, ambientColor = Color(0x2923252F), spotColor = Color(0x2923252F))
            .clip(if (filled) VaultRadius.Button else CircleShape)
            .background(container)
            .clickable(interaction, ripple(), role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(painterResource(icon), contentDescription, tint = tint, modifier = Modifier.size(26.dp))
    }
}

@ThemePreviews
@androidx.compose.runtime.Composable
private fun ButtonsPreview() = VaultPreview {
    CompositionLocalProvider(LocalContentColor provides VaultTheme.scheme.onBackground) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            VaultButton("Continue", onClick = {})
            VaultButton("Import photos", onClick = {}, leadingIcon = VaultIcons.Plus)
            VaultButton("Hide", onClick = {}, enabled = false)
            VaultButton("Retry backup", onClick = {}, style = VaultButtonStyle.Danger, leadingIcon = VaultIcons.Refresh)
            VaultButton("Loading", onClick = {}, loading = true)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                VaultOutlinedChipButton("Reset Password", onClick = {})
                VaultTextButton("Cancel", onClick = {})
                VaultIconButton(VaultIcons.MoreVert, null, onClick = {})
                VaultFab(onClick = {})
                VaultFab(onClick = {}, filled = true)
            }
        }
    }
}
