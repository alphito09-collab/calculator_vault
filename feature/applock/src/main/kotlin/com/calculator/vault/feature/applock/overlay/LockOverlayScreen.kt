package com.calculator.vault.feature.applock.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.calculator.vault.core.designsystem.component.PatternLock
import com.calculator.vault.core.designsystem.component.PinDots
import com.calculator.vault.core.designsystem.component.PinPad
import com.calculator.vault.core.designsystem.component.isLandscape
import com.calculator.vault.core.designsystem.preview.ScreenPreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultRadius
import com.calculator.vault.core.designsystem.theme.VaultTheme
import com.calculator.vault.core.model.AppIconRequest
import com.calculator.vault.core.model.AppLockAuthMethod
import com.calculator.vault.feature.applock.R

@Composable
fun LockOverlayScreen(state: LockOverlayState, appLabel: String, onIntent: (LockOverlayIntent) -> Unit) {
    val header: @Composable () -> Unit = {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AsyncImage(AppIconRequest(state.packageName), null, Modifier.size(64.dp).clip(VaultRadius.Tile))
            Spacer(Modifier.height(16.dp))
            Text(appLabel, style = VaultTheme.typography.headlineMedium, color = VaultTheme.scheme.onBackground, textAlign = TextAlign.Center)
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(
                    when {
                        state.error && state.method == AppLockAuthMethod.PATTERN -> R.string.applock_wrong_pattern
                        state.error -> R.string.applock_wrong_pin
                        state.method == AppLockAuthMethod.PATTERN -> R.string.applock_draw_pattern
                        else -> R.string.applock_enter_pin
                    },
                ),
                style = VaultTheme.typography.bodyMedium,
                color = if (state.error) VaultTheme.scheme.error else VaultTheme.scheme.onSurfaceVariant,
            )
        }
    }
    val input: @Composable (Modifier) -> Unit = { modifier ->
        when (state.method) {
            AppLockAuthMethod.PATTERN -> PatternLock(
                onPatternComplete = { onIntent(LockOverlayIntent.Pattern(it)) },
                isError = state.error,
                resetKey = state.patternResetKey,
                modifier = modifier.widthIn(max = 320.dp),
            )
            else -> Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
                PinDots(state.input.length, isError = state.error && state.input.isEmpty(), errorTrigger = state.errorTrigger)
                Spacer(Modifier.height(32.dp))
                PinPad({ onIntent(LockOverlayIntent.Digit(it)) }, { onIntent(LockOverlayIntent.Backspace) }, Modifier.widthIn(max = 320.dp))
            }
        }
    }
    if (isLandscape()) {
        Row(
            Modifier.fillMaxSize().background(VaultTheme.scheme.background).statusBarsPadding().navigationBarsPadding().padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) { header() }
            input(Modifier.weight(1f).fillMaxHeight())
        }
    } else {
        Column(
            Modifier.fillMaxSize().background(VaultTheme.scheme.background).statusBarsPadding().navigationBarsPadding().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(0.6f))
            header()
            Spacer(Modifier.weight(0.5f))
            input(Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))
        }
    }
}

@ScreenPreviews
@Composable
private fun LockOverlayPreview() = VaultPreview { LockOverlayScreen(LockOverlayState("com.example", ready = true, input = "12"), "WhatsApp", onIntent = {}) }
