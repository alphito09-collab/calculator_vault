package com.calculator.vault.feature.pin.presentation.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calculator.vault.core.designsystem.component.CalculatorKeypad
import com.calculator.vault.core.designsystem.component.ToastKind
import com.calculator.vault.core.designsystem.component.TooltipBubble
import com.calculator.vault.core.designsystem.component.VaultOutlinedChipButton
import com.calculator.vault.core.designsystem.component.VaultSnackbarHost
import com.calculator.vault.core.designsystem.component.isLandscape
import com.calculator.vault.core.designsystem.component.showToast
import com.calculator.vault.core.designsystem.preview.ScreenPreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultTheme
import com.calculator.vault.core.ui.mvi.CollectEffects
import com.calculator.vault.feature.pin.R
import com.calculator.vault.feature.pin.presentation.components.PinSlots
import com.calculator.vault.feature.pin.presentation.mvi.CreatePinEffect
import com.calculator.vault.feature.pin.presentation.mvi.CreatePinIntent
import com.calculator.vault.feature.pin.presentation.mvi.CreatePinState
import com.calculator.vault.feature.pin.presentation.mvi.CreatePinStep
import com.calculator.vault.feature.pin.presentation.mvi.CreatePinViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun CreatePinRoute(onCreated: () -> Unit, viewModel: CreatePinViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val failedText = stringResource(R.string.pin_create_failed)
    viewModel.effects.CollectEffects { effect ->
        when (effect) {
            CreatePinEffect.Created -> onCreated()
            CreatePinEffect.Failed -> snackbar.showToast(failedText, ToastKind.Error)
        }
    }
    CreatePinScreen(state, viewModel::onIntent, snackbar)
}

@Composable
fun CreatePinScreen(
    state: CreatePinState,
    onIntent: (CreatePinIntent) -> Unit,
    snackbar: SnackbarHostState = remember { SnackbarHostState() },
) {
    val landscape = isLandscape()
    Box(Modifier.fillMaxSize().background(VaultTheme.scheme.background)) {
        if (landscape) {
            Row(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(16.dp)) {
                Column(Modifier.weight(1f).fillMaxHeight().padding(end = 16.dp)) {
                    Header(state)
                    Spacer(Modifier.weight(1f))
                    Display(state, onIntent)
                }
                Keypad(state, onIntent, Modifier.weight(1.1f).fillMaxHeight())
            }
        } else {
            Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
                Header(state, Modifier.padding(horizontal = 4.dp))
                Spacer(Modifier.weight(1f))
                Display(state, onIntent, Modifier.padding(horizontal = 4.dp))
                Spacer(Modifier.height(24.dp))
                Keypad(
                    state,
                    onIntent,
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .widthIn(max = 480.dp)
                        .fillMaxWidth()
                        .weight(1.25f)
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 12.dp),
                )
            }
        }
        VaultSnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun Header(state: CreatePinState, modifier: Modifier = Modifier) {
    val text = when {
        state.mismatch -> stringResource(R.string.pin_mismatch)
        state.step == CreatePinStep.ENTER -> stringResource(R.string.pin_enter_and_press_equal)
        else -> stringResource(R.string.pin_reenter_and_confirm)
    }
    AnimatedContent(
        targetState = text,
        transitionSpec = { (fadeIn() + slideInVertically { it / 3 }) togetherWith fadeOut() },
        label = "pinHeader",
        modifier = modifier.padding(horizontal = 24.dp, vertical = 24.dp),
    ) { value ->
        Text(
            value,
            style = VaultTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Medium),
            color = if (state.mismatch) VaultTheme.scheme.error else VaultTheme.scheme.onBackground,
        )
    }
}

@Composable
private fun Display(state: CreatePinState, onIntent: (CreatePinIntent) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().padding(horizontal = 24.dp), verticalAlignment = Alignment.Bottom) {
        AnimatedVisibility(
            visible = state.step == CreatePinStep.CONFIRM || state.mismatch,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
        ) {
            VaultOutlinedChipButton(stringResource(R.string.pin_reset_password), onClick = { onIntent(CreatePinIntent.ResetPassword) })
        }
        Spacer(Modifier.weight(1f))
        PinSlots(filled = state.input.length, isError = state.mismatch, errorTrigger = state.errorTrigger)
    }
}

@Composable
private fun Keypad(state: CreatePinState, onIntent: (CreatePinIntent) -> Unit, modifier: Modifier) {
    BoxWithConstraints(modifier) {
        val spacing = 6.dp
        val rowHeight = (maxHeight - spacing * 4) / 5
        CalculatorKeypad(onKey = { onIntent(CreatePinIntent.Key(it)) }, modifier = Modifier.fillMaxSize(), spacing = spacing)
        AnimatedVisibility(
            visible = state.isComplete && !state.creating,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = rowHeight - 6.dp),
        ) {
            TooltipBubble(stringResource(R.string.pin_now_press_equal))
        }
    }
}

@ScreenPreviews
@Composable
private fun CreatePinPreview() = VaultPreview { CreatePinScreen(CreatePinState(input = "12"), onIntent = {}) }

@ScreenPreviews
@Composable
private fun CreatePinConfirmPreview() = VaultPreview {
    CreatePinScreen(CreatePinState(step = CreatePinStep.CONFIRM, input = "1234"), onIntent = {})
}

@ScreenPreviews
@Composable
private fun CreatePinMismatchPreview() = VaultPreview {
    CreatePinScreen(CreatePinState(step = CreatePinStep.CONFIRM, mismatch = true), onIntent = {})
}
