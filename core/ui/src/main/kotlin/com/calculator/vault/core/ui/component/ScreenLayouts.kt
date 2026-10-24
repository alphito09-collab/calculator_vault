package com.calculator.vault.core.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.calculator.vault.core.designsystem.component.VaultSnackbarHost
import com.calculator.vault.core.designsystem.icon.VaultIcons
import com.calculator.vault.core.designsystem.theme.Spacing
import com.calculator.vault.core.designsystem.theme.VaultTheme
import com.calculator.vault.core.ui.R

/**
 * Standard vault screen: top bar, content and a floating bottom area (button or action bar)
 * that content scrolls underneath, fading out like in the design.
 */
@Composable
fun VaultScreen(
    topBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    bottomVisible: Boolean = true,
    bottomBar: (@Composable ColumnScope.() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier.fillMaxSize().background(VaultTheme.scheme.background)) {
        Column(Modifier.fillMaxSize()) {
            topBar()
            Box(Modifier.weight(1f).fillMaxWidth(), content = content)
        }
        if (bottomBar != null) {
            AnimatedVisibility(
                visible = bottomVisible,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                BottomFade { bottomBar() }
            }
        }
        VaultSnackbarHost(
            snackbarHostState,
            Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = if (bottomBar != null) 96.dp else 16.dp),
        )
    }
}

/** Gradient from transparent to the background behind bottom actions. */
@Composable
fun BottomFade(content: @Composable ColumnScope.() -> Unit) {
    val bg = VaultTheme.scheme.background
    Column(
        Modifier
            .fillMaxWidth()
            // Fully opaque from the button down, so nothing scrolls visibly into the nav bar area.
            .background(Brush.verticalGradient(0f to Color.Transparent, 0.3f to bg.copy(alpha = 0.94f), 0.5f to bg))
            .windowInsetsPadding(WindowInsets.ime)
            .navigationBarsPadding()
            .padding(horizontal = Spacing.screen)
            .padding(top = 28.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(Modifier.widthIn(max = 520.dp), content = content)
    }
}

/** Thin warning strip shown when a screen needs internet and there is none. */
@Composable
fun OfflineBanner(visible: Boolean, modifier: Modifier = Modifier) {
    AnimatedVisibility(visible, modifier, enter = slideInVertically() + fadeIn(), exit = slideOutVertically() + fadeOut()) {
        androidx.compose.foundation.layout.Row(
            Modifier
                .fillMaxWidth()
                .background(VaultTheme.scheme.errorContainer)
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(painterResource(VaultIcons.WifiOff), null, tint = VaultTheme.scheme.onErrorContainer, modifier = Modifier.padding(end = 10.dp))
            Text(stringResource(R.string.ui_offline), style = VaultTheme.typography.bodyMedium, color = VaultTheme.scheme.onErrorContainer)
        }
    }
}
