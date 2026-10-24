package com.calculator.vault.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.calculator.vault.core.designsystem.R
import com.calculator.vault.core.designsystem.icon.VaultIcons
import com.calculator.vault.core.designsystem.preview.ThemePreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultRadius
import com.calculator.vault.core.designsystem.theme.VaultTheme

/**
 * Light-blue promo card with a close badge on its top-left corner ("Enable Cloud backup").
 * Without [onClick] it is purely informational (no chevron); without [onDismiss] it has no badge.
 */
@Composable
fun InfoBanner(
    title: String,
    message: String,
    @DrawableRes icon: Int,
    onClick: (() -> Unit)?,
    onDismiss: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Box(modifier.padding(top = 6.dp, start = 6.dp)) {
        VaultCard(onClick = onClick, color = VaultTheme.colors.banner, shape = VaultRadius.Banner, modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(horizontal = 18.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(icon), null, tint = VaultTheme.scheme.primary, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(title, style = VaultTheme.typography.titleSmall, color = VaultTheme.scheme.onSurface)
                    Text(message, style = VaultTheme.typography.bodySmall, color = VaultTheme.scheme.onSurfaceVariant)
                }
                if (onClick != null) {
                    Icon(
                        painterResource(VaultIcons.ChevronRight),
                        null,
                        tint = VaultTheme.scheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }
        if (onDismiss != null) DismissBadge(onDismiss)
    }
}

@Composable
private fun DismissBadge(onDismiss: () -> Unit) {
    Box(
        Modifier
            .offset((-6).dp, (-6).dp)
            .size(24.dp)
            .shadow(3.dp, CircleShape)
            .clip(CircleShape)
            .background(VaultTheme.scheme.background)
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painterResource(VaultIcons.CloseSmall),
            stringResource(R.string.ds_dismiss),
            tint = VaultTheme.colors.navIcon,
            modifier = Modifier.size(13.dp),
        )
    }
}

@ThemePreviews
@Composable
private fun BannerPreview() = VaultPreview {
    InfoBanner(
        title = "Enable Cloud backup",
        message = "Secure backup for your private photos",
        icon = VaultIcons.Cloud,
        onClick = {},
        onDismiss = {},
        modifier = Modifier.padding(16.dp),
    )
}

@ThemePreviews
@Composable
private fun InfoOnlyBannerPreview() = VaultPreview {
    InfoBanner(
        title = "Forgot your PIN?",
        message = "Type 7860786 on the calculator and press = to set a new PIN.",
        icon = VaultIcons.Key,
        onClick = null,
        onDismiss = null,
        modifier = Modifier.padding(16.dp),
    )
}
