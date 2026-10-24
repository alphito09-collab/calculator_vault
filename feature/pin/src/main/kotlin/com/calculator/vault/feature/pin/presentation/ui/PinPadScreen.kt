package com.calculator.vault.feature.pin.presentation.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.calculator.vault.core.designsystem.component.PinDots
import com.calculator.vault.core.designsystem.component.PinPad
import com.calculator.vault.core.designsystem.component.VaultTopBar
import com.calculator.vault.core.designsystem.component.isLandscape
import com.calculator.vault.core.designsystem.preview.ScreenPreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultTheme
import com.calculator.vault.core.ui.mvi.CollectEffects
import com.calculator.vault.feature.pin.R
import com.calculator.vault.feature.pin.presentation.mvi.PinPadEffect
import com.calculator.vault.feature.pin.presentation.mvi.PinPadError
import com.calculator.vault.feature.pin.presentation.mvi.PinPadIntent
import com.calculator.vault.feature.pin.presentation.mvi.PinPadMode
import com.calculator.vault.feature.pin.presentation.mvi.PinPadState
import com.calculator.vault.feature.pin.presentation.mvi.PinPadStep
import com.calculator.vault.feature.pin.presentation.mvi.PinPadViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun PinPadRoute(
    mode: PinPadMode,
    onDone: () -> Unit,
    onClose: () -> Unit,
    viewModel: PinPadViewModel = koinViewModel(key = "pinpad-$mode") { parametersOf(mode) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    viewModel.effects.CollectEffects { effect ->
        when (effect) {
            PinPadEffect.Done -> onDone()
            PinPadEffect.Close -> onClose()
        }
    }
    PinPadScreen(state, viewModel::onIntent)
}

@Composable
fun PinPadScreen(state: PinPadState, onIntent: (PinPadIntent) -> Unit) {
    Column(Modifier.fillMaxSize().background(VaultTheme.scheme.background)) {
        VaultTopBar(title = "", onNavigationClick = { onIntent(PinPadIntent.Close) })
        if (isLandscape()) {
            Row(Modifier.fillMaxSize().navigationBarsPadding().padding(horizontal = 24.dp)) {
                Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    Texts(state)
                    Spacer(Modifier.height(32.dp))
                    PinDots(filled = state.input.length, isError = state.error != null && state.input.isEmpty(), errorTrigger = state.errorTrigger)
                }
                PinPad(
                    onDigit = { onIntent(PinPadIntent.Digit(it)) },
                    onBackspace = { onIntent(PinPadIntent.Backspace) },
                    modifier = Modifier.weight(1f).align(Alignment.CenterVertically).widthIn(max = 300.dp),
                )
            }
        } else {
            Column(
                Modifier.fillMaxSize().navigationBarsPadding().padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.weight(0.4f))
                Texts(state)
                Spacer(Modifier.height(48.dp))
                PinDots(filled = state.input.length, isError = state.error != null && state.input.isEmpty(), errorTrigger = state.errorTrigger)
                Spacer(Modifier.weight(0.6f))
                PinPad(
                    onDigit = { onIntent(PinPadIntent.Digit(it)) },
                    onBackspace = { onIntent(PinPadIntent.Backspace) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                )
            }
        }
    }
}

@Composable
private fun Texts(state: PinPadState) {
    val (title, subtitle) = when (state.step) {
        PinPadStep.CURRENT -> stringResource(R.string.pin_current_title) to stringResource(R.string.pin_current_subtitle)
        PinPadStep.NEW -> stringResource(R.string.pin_new_title) to stringResource(R.string.pin_new_subtitle)
        PinPadStep.CONFIRM -> stringResource(R.string.pin_confirm_title) to stringResource(R.string.pin_confirm_subtitle)
    }
    val error = when (state.error) {
        PinPadError.WRONG_CURRENT -> stringResource(R.string.pin_wrong_current)
        PinPadError.MISMATCH -> stringResource(R.string.pin_mismatch_short)
        PinPadError.SAVE_FAILED -> stringResource(R.string.pin_create_failed)
        null -> null
    }
    AnimatedContent(
        targetState = state.step,
        transitionSpec = { (slideInHorizontally { it / 3 } + fadeIn()) togetherWith (slideOutHorizontally { -it / 3 } + fadeOut()) },
        label = "pinStep",
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = VaultTheme.typography.headlineMedium, color = VaultTheme.scheme.onBackground, textAlign = TextAlign.Center)
            Spacer(Modifier.height(10.dp))
            Text(
                error ?: subtitle,
                style = VaultTheme.typography.bodyMedium,
                color = if (error != null) VaultTheme.scheme.error else VaultTheme.scheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@ScreenPreviews
@Composable
private fun PinPadPreview() = VaultPreview { PinPadScreen(PinPadState(input = "123"), onIntent = {}) }

@ScreenPreviews
@Composable
private fun PinPadErrorPreview() = VaultPreview {
    PinPadScreen(PinPadState(mode = PinPadMode.CHANGE, step = PinPadStep.CONFIRM, error = PinPadError.MISMATCH), onIntent = {})
}
