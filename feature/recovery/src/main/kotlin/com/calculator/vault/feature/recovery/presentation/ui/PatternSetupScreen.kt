package com.calculator.vault.feature.recovery.presentation.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calculator.vault.core.designsystem.component.PatternLock
import com.calculator.vault.core.designsystem.component.VaultTopBar
import com.calculator.vault.core.designsystem.component.isLandscape
import com.calculator.vault.core.designsystem.preview.ScreenPreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultTheme
import com.calculator.vault.core.ui.mvi.CollectEffects
import com.calculator.vault.feature.recovery.R
import com.calculator.vault.feature.recovery.domain.usecase.PatternTarget
import com.calculator.vault.feature.recovery.presentation.mvi.PatternMessage
import com.calculator.vault.feature.recovery.presentation.mvi.PatternSetupEffect
import com.calculator.vault.feature.recovery.presentation.mvi.PatternSetupIntent
import com.calculator.vault.feature.recovery.presentation.mvi.PatternSetupState
import com.calculator.vault.feature.recovery.presentation.mvi.PatternSetupViewModel
import com.calculator.vault.feature.recovery.presentation.mvi.PatternStep
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun PatternSetupRoute(
    target: PatternTarget,
    onSaved: () -> Unit,
    onClose: () -> Unit,
    viewModel: PatternSetupViewModel = koinViewModel(key = "pattern-$target") { parametersOf(target) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    viewModel.effects.CollectEffects { effect ->
        when (effect) {
            PatternSetupEffect.Saved -> onSaved()
            PatternSetupEffect.Close -> onClose()
        }
    }
    PatternSetupScreen(state, viewModel::onIntent)
}

@Composable
fun PatternSetupScreen(state: PatternSetupState, onIntent: (PatternSetupIntent) -> Unit) {
    val title = stringResource(if (state.step == PatternStep.DRAW) R.string.recovery_pattern_title else R.string.recovery_pattern_confirm_title)
    val message = stringResource(
        when (state.message) {
            PatternMessage.CONNECT_AT_LEAST -> R.string.recovery_pattern_hint
            PatternMessage.DRAW_AGAIN -> R.string.recovery_pattern_again
            PatternMessage.TOO_SHORT -> R.string.recovery_pattern_too_short
            PatternMessage.MISMATCH -> R.string.recovery_pattern_mismatch
        },
    )
    Column(Modifier.fillMaxSize().background(VaultTheme.scheme.background)) {
        VaultTopBar(title = "", onNavigationClick = { onIntent(PatternSetupIntent.Back) })
        val header: @Composable () -> Unit = {
            AnimatedContent(title to message, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "patternText") { (t, m) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(t, style = VaultTheme.typography.headlineMedium, color = VaultTheme.scheme.onBackground, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        m,
                        style = VaultTheme.typography.bodyMedium,
                        color = if (state.isError) VaultTheme.scheme.error else VaultTheme.scheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
        if (isLandscape()) {
            Row(Modifier.fillMaxSize().navigationBarsPadding().padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { header() }
                PatternLock(
                    onPatternComplete = { onIntent(PatternSetupIntent.Drawn(it)) },
                    isError = state.isError,
                    resetKey = state.resetKey,
                    modifier = Modifier.weight(1f).fillMaxHeight().padding(12.dp),
                )
            }
        } else {
            Column(Modifier.fillMaxSize().navigationBarsPadding().padding(horizontal = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(60.dp))
                header()
                Spacer(Modifier.weight(1f))
                PatternLock(
                    onPatternComplete = { onIntent(PatternSetupIntent.Drawn(it)) },
                    isError = state.isError,
                    resetKey = state.resetKey,
                    modifier = Modifier.widthIn(max = 340.dp),
                )
                Spacer(Modifier.weight(0.6f))
            }
        }
    }
}

@ScreenPreviews
@Composable
private fun PatternSetupPreview() = VaultPreview { PatternSetupScreen(PatternSetupState(), onIntent = {}) }
