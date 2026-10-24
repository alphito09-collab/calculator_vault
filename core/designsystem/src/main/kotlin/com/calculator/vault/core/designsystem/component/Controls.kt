package com.calculator.vault.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.calculator.vault.core.designsystem.R
import com.calculator.vault.core.designsystem.icon.VaultIcons
import com.calculator.vault.core.designsystem.preview.ThemePreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultTheme

/** Blue-track switch with a white thumb and no thumb icon, matching the design. */
@Composable
fun VaultSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = VaultTheme.scheme.primary,
            checkedBorderColor = Color.Transparent,
            uncheckedThumbColor = Color.White,
            uncheckedTrackColor = VaultTheme.colors.switchTrackOff,
            uncheckedBorderColor = Color.Transparent,
            disabledCheckedTrackColor = VaultTheme.scheme.primary.copy(alpha = 0.4f),
            disabledCheckedThumbColor = Color.White,
            disabledUncheckedTrackColor = VaultTheme.colors.switchTrackOff.copy(alpha = 0.5f),
            disabledUncheckedThumbColor = Color.White,
            disabledUncheckedBorderColor = Color.Transparent,
        ),
    )
}

@Composable
fun VaultRadioButton(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    RadioButton(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = RadioButtonDefaults.colors(
            selectedColor = VaultTheme.scheme.primary,
            unselectedColor = VaultTheme.colors.cardBorder.copy(alpha = 1f).let {
                if (VaultTheme.colors.isDark) Color(0xFF5B5F6B) else Color(0xFFC9C7C1)
            },
        ),
    )
}

enum class SelectionIndicatorStyle {
    /** Translucent ring over thumbnails. */
    OverMedia,

    /** Grey filled circle on list rows. */
    OnSurface,
}

/** Round selection badge: blue with a white check when selected. */
@Composable
fun SelectionIndicator(
    selected: Boolean,
    modifier: Modifier = Modifier,
    style: SelectionIndicatorStyle = SelectionIndicatorStyle.OverMedia,
    size: Dp = 26.dp,
) {
    val fill by animateColorAsState(
        targetValue = when {
            selected -> VaultTheme.scheme.primary
            style == SelectionIndicatorStyle.OverMedia -> Color.Black.copy(alpha = 0.25f)
            else -> if (VaultTheme.colors.isDark) Color(0xFF3A3E49) else Color(0xFFE2E2E2)
        },
        label = "selectionFill",
    )
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(fill)
            .then(
                if (style == SelectionIndicatorStyle.OverMedia) Modifier.border(1.5.dp, Color.White, CircleShape)
                else Modifier,
            ),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(
            visible = selected,
            enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)),
            exit = scaleOut(),
        ) {
            Icon(
                painter = painterResource(VaultIcons.Check),
                contentDescription = stringResource(R.string.ds_cd_selected),
                tint = Color.White,
                modifier = Modifier.size(size * 0.62f),
            )
        }
    }
}

@ThemePreviews
@Composable
private fun ControlsPreview() = VaultPreview {
    Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        VaultSwitch(checked = true, onCheckedChange = {})
        VaultSwitch(checked = false, onCheckedChange = {})
        VaultRadioButton(selected = true, onClick = {})
        VaultRadioButton(selected = false, onClick = {})
        SelectionIndicator(selected = true)
        SelectionIndicator(selected = false)
        SelectionIndicator(selected = false, style = SelectionIndicatorStyle.OnSurface)
    }
}
