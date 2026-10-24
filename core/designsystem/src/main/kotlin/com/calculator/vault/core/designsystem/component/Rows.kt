package com.calculator.vault.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.calculator.vault.core.designsystem.icon.VaultIcons
import com.calculator.vault.core.designsystem.preview.ThemePreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.Spacing
import com.calculator.vault.core.designsystem.theme.VaultTheme

/** Trailing element of a [SettingsRow]. */
sealed interface RowTrailing {
    data object Chevron : RowTrailing
    data class Value(val text: String, val showChevron: Boolean = true) : RowTrailing
    data class Toggle(val checked: Boolean, val onCheckedChange: (Boolean) -> Unit) : RowTrailing
    data class Radio(val selected: Boolean) : RowTrailing
    data class Check(val checked: Boolean, val showChevron: Boolean = false) : RowTrailing
    data object None : RowTrailing
}

/**
 * The list row used throughout Settings, App Lock and Cloud Backup:
 * leading icon, title (+ optional subtitle) and a trailing chevron/value/switch/radio.
 */
@Composable
fun SettingsRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    @DrawableRes icon: Int? = null,
    iconTint: Color = VaultTheme.scheme.secondary,
    trailing: RowTrailing = RowTrailing.Chevron,
    enabled: Boolean = true,
    showDivider: Boolean = false,
    titleColor: Color = VaultTheme.scheme.onSurface,
    onClick: (() -> Unit)? = when (trailing) {
        is RowTrailing.Toggle -> ({ trailing.onCheckedChange(!trailing.checked) })
        else -> null
    },
) {
    Column(modifier.alpha(if (enabled) 1f else 0.45f)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 58.dp)
                .then(
                    if (onClick != null) {
                        Modifier.clickable(enabled = enabled, role = Role.Button, onClick = onClick)
                    } else Modifier,
                )
                .padding(horizontal = Spacing.md, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when (trailing) {
                is RowTrailing.Radio -> {
                    VaultRadioButton(selected = trailing.selected, onClick = null, enabled = enabled)
                    Spacer(Modifier.width(Spacing.xs))
                }
                else -> if (icon != null) {
                    Icon(painterResource(icon), null, tint = iconTint, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(Spacing.sm))
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    title,
                    style = VaultTheme.text.rowTitle,
                    color = titleColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle != null) {
                    Text(subtitle, style = VaultTheme.typography.bodySmall, color = VaultTheme.scheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.width(Spacing.xs))
            when (trailing) {
                RowTrailing.Chevron -> Chevron()
                is RowTrailing.Value -> {
                    Text(trailing.text, style = VaultTheme.text.rowTitle, color = VaultTheme.scheme.primary)
                    if (trailing.showChevron) {
                        Spacer(Modifier.width(6.dp))
                        Chevron()
                    }
                }
                is RowTrailing.Toggle -> VaultSwitch(trailing.checked, trailing.onCheckedChange, enabled = enabled)
                is RowTrailing.Check -> {
                    if (trailing.checked) SelectionIndicator(true, size = 20.dp, style = SelectionIndicatorStyle.OnSurface)
                    if (trailing.showChevron) {
                        Spacer(Modifier.width(10.dp))
                        Chevron()
                    }
                }
                is RowTrailing.Radio, RowTrailing.None -> Unit
            }
        }
        if (showDivider) RowDivider(Modifier.padding(start = if (icon != null || trailing is RowTrailing.Radio) 52.dp else Spacing.md, end = Spacing.md))
    }
}

@Composable
private fun Chevron() {
    Icon(
        painterResource(VaultIcons.ChevronRight),
        contentDescription = null,
        tint = VaultTheme.colors.iconTileContent,
        modifier = Modifier.size(15.dp),
    )
}

@Composable
fun RowDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier, thickness = 1.dp, color = VaultTheme.colors.divider)
}

/** Grey caption above a group ("Tools", "Lock after", "Backup using"). */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier, trailing: @Composable (() -> Unit)? = null) {
    Row(modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text,
            style = VaultTheme.typography.labelSmall,
            color = VaultTheme.colors.sectionLabel,
            modifier = Modifier.weight(1f),
        )
        trailing?.invoke()
    }
}

/** 44dp rounded square holding a tinted icon (row leading visuals, empty states). */
@Composable
fun IconTile(
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier,
    tint: Color = VaultTheme.scheme.secondary,
    background: Color = VaultTheme.scheme.primaryContainer,
    size: androidx.compose.ui.unit.Dp = 44.dp,
    iconSize: androidx.compose.ui.unit.Dp = 22.dp,
) {
    Box(
        modifier
            .size(size)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(size * 0.32f))
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        Icon(painterResource(icon), null, tint = tint, modifier = Modifier.size(iconSize))
    }
}

@ThemePreviews
@Composable
private fun RowsPreview() = VaultPreview {
    Column(Modifier.padding(20.dp)) {
        SectionLabel("Authentication")
        VaultCardColumn {
            SettingsRow("App lock", icon = VaultIcons.LockKeyhole, trailing = RowTrailing.Value("On"), showDivider = true, onClick = {})
            SettingsRow("Intruder detection", icon = VaultIcons.ShieldCheckAlt, onClick = {}, showDivider = true)
            SettingsRow("Notification", icon = VaultIcons.Bell, trailing = RowTrailing.Toggle(true) {}, showDivider = true)
            SettingsRow("Immediately", trailing = RowTrailing.Radio(true), onClick = {})
        }
    }
}
