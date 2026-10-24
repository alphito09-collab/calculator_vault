package com.calculator.vault.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.calculator.vault.core.designsystem.R
import com.calculator.vault.core.designsystem.icon.VaultIcons
import com.calculator.vault.core.designsystem.preview.ThemePreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultRadius
import com.calculator.vault.core.designsystem.theme.VaultTheme

data class BarAction(val label: String, val onClick: () -> Unit, val enabled: Boolean = true, val destructive: Boolean = false)

/** Floating white pill with evenly spaced text actions ("Share  Delete  Unhide"). */
@Composable
fun FloatingActionBar(actions: List<BarAction>, modifier: Modifier = Modifier) {
    Row(
        modifier
            .widthIn(max = 480.dp)
            .fillMaxWidth()
            .shadow(16.dp, VaultRadius.Card, ambientColor = Color(0x1423252F), spotColor = Color(0x2923252F))
            .clip(VaultRadius.Card)
            .background(VaultTheme.scheme.surface)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        actions.forEach { action ->
            Text(
                text = action.label,
                style = VaultTheme.text.rowTitle.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium),
                color = when {
                    !action.enabled -> VaultTheme.scheme.onSurface.copy(alpha = 0.35f)
                    action.destructive -> VaultTheme.scheme.error
                    else -> VaultTheme.scheme.onSurface
                },
                modifier = Modifier
                    .clip(VaultRadius.Card)
                    .clickable(enabled = action.enabled, role = Role.Button, onClick = action.onClick)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            )
        }
    }
}

/** Distance from the bottom edge (above the navigation bar) to the centre of the "+" button. */
val BottomBarFabCenterFromBottom = 88.dp

/**
 * Bottom navigation pill: Home and Settings with a raised round "+" in the middle.
 * The center button opens the radial add menu owned by the home feature.
 */
@Composable
fun VaultBottomBar(
    homeSelected: Boolean,
    onHomeClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    addContent: (@Composable () -> Unit)? = null,
) {
    Box(
        modifier
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(bottom = 12.dp)
            .widthIn(max = 520.dp)
            .fillMaxWidth()
            .height(116.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(80.dp)
                .shadow(16.dp, VaultRadius.Card, ambientColor = Color(0x1423252F), spotColor = Color(0x1F23252F))
                .clip(VaultRadius.Card)
                .background(VaultTheme.scheme.surface)
                .padding(horizontal = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            NavTab(
                selected = homeSelected,
                selectedIcon = VaultIcons.HomeFilled,
                unselectedIcon = VaultIcons.HomeOutline,
                label = stringResource(R.string.ds_home),
                onClick = onHomeClick,
            )
            NavTab(
                selected = !homeSelected,
                selectedIcon = VaultIcons.SettingsFilled,
                unselectedIcon = VaultIcons.SettingsOutline,
                label = stringResource(R.string.ds_settings),
                onClick = onSettingsClick,
            )
        }
        Box(Modifier.align(Alignment.TopCenter).offset(y = 8.dp)) {
            addContent?.invoke() ?: VaultFab(onClick = onAddClick, contentDescription = stringResource(R.string.ds_add))
        }
    }
}

@Composable
private fun NavTab(
    selected: Boolean,
    @DrawableRes selectedIcon: Int,
    @DrawableRes unselectedIcon: Int,
    label: String,
    onClick: () -> Unit,
) {
    val bg = if (selected) VaultTheme.scheme.primaryContainer else Color.Transparent
    Column(
        Modifier
            .size(width = 64.dp, height = 44.dp)
            .clip(VaultRadius.Pill)
            .background(bg)
            .clickable(role = Role.Tab, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painterResource(if (selected) selectedIcon else unselectedIcon),
            contentDescription = label,
            tint = if (selected) VaultTheme.scheme.primary else VaultTheme.colors.navIcon,
            modifier = Modifier.size(24.dp),
        )
    }
}

@ThemePreviews
@Composable
private fun ActionBarsPreview() = VaultPreview {
    Column(Modifier.padding(vertical = 16.dp)) {
        FloatingActionBar(
            listOf(BarAction("Share", {}), BarAction("Delete", {}), BarAction("Unhide", {})),
            Modifier.padding(horizontal = 20.dp),
        )
        VaultBottomBar(homeSelected = true, onHomeClick = {}, onSettingsClick = {}, onAddClick = {})
    }
}

