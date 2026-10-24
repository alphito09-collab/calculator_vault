package com.calculator.vault.feature.calculator.presentation.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calculator.vault.core.designsystem.component.CalculatorKeypad
import com.calculator.vault.core.designsystem.component.ToastKind
import com.calculator.vault.core.designsystem.component.VaultIconButton
import com.calculator.vault.core.designsystem.component.VaultSnackbarHost
import com.calculator.vault.core.designsystem.component.isLandscape
import com.calculator.vault.core.designsystem.component.showToast
import com.calculator.vault.core.designsystem.icon.VaultIcons
import com.calculator.vault.core.designsystem.preview.ScreenPreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultTheme
import com.calculator.vault.core.model.RecoveryMethod
import com.calculator.vault.core.ui.biometric.BiometricResult
import com.calculator.vault.core.ui.biometric.rememberBiometricPrompter
import com.calculator.vault.core.ui.component.ExitAppDialog
import com.calculator.vault.core.ui.mvi.CollectEffects
import com.calculator.vault.feature.calculator.R
import com.calculator.vault.feature.calculator.domain.engine.CalcState
import com.calculator.vault.feature.calculator.presentation.components.CalculatorDisplay
import com.calculator.vault.feature.calculator.presentation.components.ForgotPinDialog
import com.calculator.vault.feature.calculator.presentation.mvi.CalculatorEffect
import com.calculator.vault.feature.calculator.presentation.mvi.CalculatorIntent
import com.calculator.vault.feature.calculator.presentation.mvi.CalculatorState
import com.calculator.vault.feature.calculator.presentation.mvi.CalculatorViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun CalculatorRoute(
    onOpenVault: () -> Unit,
    onForgotPin: (RecoveryMethod) -> Unit,
    onResetPin: () -> Unit,
    viewModel: CalculatorViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val activity = LocalActivity.current
    val prompter = rememberBiometricPrompter()
    val snackbar = remember { SnackbarHostState() }
    val promptTitle = stringResource(R.string.calculator_fingerprint_title)
    val verifyTitle = stringResource(R.string.calculator_verify_fingerprint)
    val promptCancel = stringResource(R.string.calculator_use_pin)
    val unavailable = stringResource(R.string.calculator_fingerprint_changed)
    viewModel.effects.CollectEffects { effect ->
        when (effect) {
            CalculatorEffect.OpenVault -> onOpenVault()
            is CalculatorEffect.OpenForgotPin -> onForgotPin(effect.method)
            CalculatorEffect.OpenResetPin -> onResetPin()
            CalculatorEffect.ExitApp -> activity?.finish()
            CalculatorEffect.FingerprintUnavailable -> snackbar.showToast(unavailable, ToastKind.Info)
            is CalculatorEffect.PromptFingerprint -> {
                val title = if (effect.forReset) verifyTitle else promptTitle
                prompter.authenticate(title, null, promptCancel, effect.cipher) { result ->
                    if (result is BiometricResult.Success) {
                        viewModel.onIntent(CalculatorIntent.FingerprintSucceeded(result.cipher, effect.forReset))
                    }
                }
            }
        }
    }
    BackHandler { viewModel.onIntent(CalculatorIntent.BackPressed) }
    CalculatorScreen(state, viewModel::onIntent, snackbar)
}

@Composable
fun CalculatorScreen(
    state: CalculatorState,
    onIntent: (CalculatorIntent) -> Unit,
    snackbar: SnackbarHostState = remember { SnackbarHostState() },
) {
    Box(Modifier.fillMaxSize().background(VaultTheme.scheme.background)) {
        if (isLandscape()) {
            Row(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(16.dp)) {
                Column(Modifier.weight(1f).fillMaxHeight().padding(end = 20.dp)) {
                    FingerprintButton(state, onIntent)
                    Spacer(Modifier.weight(1f))
                    CalculatorDisplay(state.calc.subtitle, state.calc.display)
                }
                Keypad(onIntent, Modifier.weight(1.15f).fillMaxHeight())
            }
        } else {
            Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
                Box(Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 8.dp), contentAlignment = Alignment.CenterStart) {
                    FingerprintButton(state, onIntent)
                }
                Spacer(Modifier.weight(1f))
                CalculatorDisplay(state.calc.subtitle, state.calc.display, Modifier.padding(horizontal = 28.dp))
                Spacer(Modifier.height(20.dp))
                Keypad(
                    onIntent,
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .widthIn(max = 480.dp)
                        .fillMaxWidth()
                        .weight(1.5f)
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 12.dp),
                )
            }
        }
        VaultSnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }
    if (state.showExitDialog) {
        ExitAppDialog(onExit = { onIntent(CalculatorIntent.ConfirmExit) }, onDismiss = { onIntent(CalculatorIntent.DismissExit) })
    }
    if (state.forgotPinMethods.isNotEmpty()) {
        ForgotPinDialog(
            methods = state.forgotPinMethods,
            onChoose = { onIntent(CalculatorIntent.ForgotPinMethodChosen(it)) },
            onDismiss = { onIntent(CalculatorIntent.DismissForgotPin) },
        )
    }
}

@Composable
private fun FingerprintButton(state: CalculatorState, onIntent: (CalculatorIntent) -> Unit) {
    AnimatedVisibility(state.fingerprintAvailable, enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut()) {
        VaultIconButton(
            icon = VaultIcons.Fingerprint,
            contentDescription = stringResource(R.string.calculator_fingerprint_title),
            onClick = { onIntent(CalculatorIntent.FingerprintClicked) },
            tint = VaultTheme.scheme.primary,
            iconSize = 26.dp,
        )
    }
}

@Composable
private fun Keypad(onIntent: (CalculatorIntent) -> Unit, modifier: Modifier) {
    CalculatorKeypad(
        onKey = { onIntent(CalculatorIntent.Key(it)) },
        onEqualsLongPress = { onIntent(CalculatorIntent.EqualsLongPressed) },
        modifier = modifier,
    )
}

@ScreenPreviews
@Composable
private fun CalculatorPreview() = VaultPreview {
    CalculatorScreen(CalculatorState(calc = CalcState(entry = "0"), fingerprintAvailable = true), onIntent = {})
}
