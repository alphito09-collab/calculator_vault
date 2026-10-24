package com.calculator.vault.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.calculator.vault.core.designsystem.R
import com.calculator.vault.core.designsystem.icon.VaultIcons
import com.calculator.vault.core.designsystem.preview.ThemePreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.Spacing
import com.calculator.vault.core.designsystem.theme.VaultTheme

enum class NavigationIcon(@DrawableRes val icon: Int) { Back(VaultIcons.Back), Close(VaultIcons.Close) }

/** 56dp app bar: optional back/close icon, Roboto-medium title and trailing actions. */
@Composable
fun VaultTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: NavigationIcon? = NavigationIcon.Back,
    onNavigationClick: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
) {
    VaultTopBar(AnnotatedString(title), modifier, navigationIcon, onNavigationClick, actions)
}

@Composable
fun VaultTopBar(
    title: AnnotatedString,
    modifier: Modifier = Modifier,
    navigationIcon: NavigationIcon? = NavigationIcon.Back,
    onNavigationClick: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
) {
    CompositionLocalProvider(LocalContentColor provides VaultTheme.scheme.onBackground) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .height(Spacing.topBarHeight)
                .padding(start = if (navigationIcon == null) Spacing.screen else 8.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (navigationIcon != null) {
                VaultIconButton(
                    icon = navigationIcon.icon,
                    contentDescription = stringResource(
                        if (navigationIcon == NavigationIcon.Back) R.string.ds_cd_back else R.string.ds_cd_close,
                    ),
                    onClick = onNavigationClick,
                )
                Spacer(Modifier.width(4.dp))
            }
            Text(
                text = title,
                style = VaultTheme.typography.titleLarge.copy(fontSize = VaultTheme.typography.titleLarge.fontSize * 1.1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End, content = actions)
        }
    }
}

/** Top bar while items are selected: "✕  4 Selected            Select all". */
@Composable
fun SelectionTopBar(
    selectedCount: Int,
    allSelected: Boolean,
    onClose: () -> Unit,
    onToggleSelectAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        targetState = selectedCount,
        transitionSpec = {
            (slideInVertically { -it / 2 } + fadeIn()) togetherWith (slideOutVertically { it / 2 } + fadeOut())
        },
        label = "selectionCount",
        modifier = modifier,
    ) { count ->
        VaultTopBar(
            title = stringResource(R.string.ds_selected_count, count),
            navigationIcon = NavigationIcon.Close,
            onNavigationClick = onClose,
        ) {
            VaultTextButton(
                text = stringResource(if (allSelected) R.string.ds_deselect_all else R.string.ds_select_all),
                onClick = onToggleSelectAll,
                small = true,
            )
        }
    }
}

/** "Calculator **Vault**" — the second word in the accent colour. */
@Composable
fun brandTitle(first: String, accent: String): AnnotatedString {
    val color = VaultTheme.scheme.primary
    return buildAnnotatedString {
        append(first)
        append(' ')
        withStyle(SpanStyle(color = color)) { append(accent) }
    }
}

@ThemePreviews
@Composable
private fun TopBarsPreview() = VaultPreview {
    Column {
        VaultTopBar(brandTitle("Calculator", "Vault"), navigationIcon = null)
        VaultTopBar("Photos") { VaultIconButton(VaultIcons.MoreVert, null, onClick = {}) }
        VaultTopBar("App lock") { VaultIconButton(VaultIcons.Search, null, onClick = {}) }
        SelectionTopBar(selectedCount = 4, allSelected = false, onClose = {}, onToggleSelectAll = {})
        Spacer(Modifier.height(8.dp))
    }
}
