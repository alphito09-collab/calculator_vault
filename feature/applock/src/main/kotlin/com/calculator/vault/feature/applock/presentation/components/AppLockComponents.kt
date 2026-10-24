package com.calculator.vault.feature.applock.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.calculator.vault.core.designsystem.component.VaultButton
import com.calculator.vault.core.designsystem.component.VaultButtonStyle
import com.calculator.vault.core.designsystem.component.VaultCard
import com.calculator.vault.core.designsystem.icon.VaultIcons
import com.calculator.vault.core.designsystem.preview.ThemePreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultTheme
import com.calculator.vault.core.model.AppIconRequest
import com.calculator.vault.core.model.InstalledApp

/** App row: round icon, name, lock that turns blue (with a bounce) when locked. */
@Composable
fun AppRow(app: InstalledApp, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    val tint by animateColorAsState(if (app.isLocked) VaultTheme.scheme.primary else VaultTheme.colors.cardBorder, label = "lockTint")
    val scale by animateFloatAsState(if (app.isLocked) 1f else 0.9f, spring(dampingRatio = Spring.DampingRatioHighBouncy), label = "lockScale")
    Row(
        modifier.fillMaxWidth().clickable(onClick = onToggle).padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(AppIconRequest(app.packageName), app.label, Modifier.size(44.dp).clip(CircleShape))
        Spacer(Modifier.width(14.dp))
        Text(app.label, style = VaultTheme.text.rowTitle, color = VaultTheme.scheme.onBackground, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
        Icon(
            painterResource(if (app.isLocked) VaultIcons.LockSolid else VaultIcons.LockOpen),
            null,
            tint = tint,
            modifier = Modifier.size(24.dp).graphicsLayer { scaleX = scale; scaleY = scale },
        )
    }
}

/** Explains and links to the special permissions App Lock needs. */
@Composable
fun PermissionCard(title: String, message: String, action: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    VaultCard(modifier.fillMaxWidth(), color = VaultTheme.colors.banner) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(VaultIcons.ShieldAlert), null, tint = VaultTheme.scheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text(title, style = VaultTheme.typography.titleMedium, color = VaultTheme.scheme.onSurface)
            }
            Text(message, style = VaultTheme.typography.bodyMedium, color = VaultTheme.scheme.onSurfaceVariant)
            VaultButton(action, onClick, style = VaultButtonStyle.Primary)
        }
    }
}

/** Coach mark pointing at the first lock ("Tap on lock to add any app in App Lock"). */
@Composable
fun LockHint(text: String, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.55f))
            .clickable(onClick = onDismiss)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Text(text, style = VaultTheme.text.rowTitle, color = androidx.compose.ui.graphics.Color.White)
    }
}

@ThemePreviews
@Composable
private fun AppLockComponentsPreview() = VaultPreview {
    Column {
        AppRow(InstalledApp("com.android.contacts", "Contacts", true), {})
        AppRow(InstalledApp("com.google", "Google", false), {})
        PermissionCard("Allow usage access", "App Lock needs to see which app is open.", "Grant access", {}, Modifier.padding(20.dp))
    }
}
