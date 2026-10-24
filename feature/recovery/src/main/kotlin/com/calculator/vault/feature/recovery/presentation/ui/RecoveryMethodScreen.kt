package com.calculator.vault.feature.recovery.presentation.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calculator.vault.core.designsystem.component.NavigationIcon
import com.calculator.vault.core.designsystem.component.ToastKind
import com.calculator.vault.core.designsystem.component.VaultButton
import com.calculator.vault.core.designsystem.component.VaultTextButton
import com.calculator.vault.core.designsystem.component.VaultTopBar
import com.calculator.vault.core.designsystem.component.showToast
import com.calculator.vault.core.designsystem.icon.VaultIcons
import com.calculator.vault.core.designsystem.preview.ScreenPreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultTheme
import com.calculator.vault.core.model.RecoveryOptions
import com.calculator.vault.core.model.SecurityQuestion
import com.calculator.vault.core.ui.biometric.BiometricResult
import com.calculator.vault.core.ui.biometric.rememberBiometricPrompter
import com.calculator.vault.core.ui.component.VaultScreen
import com.calculator.vault.core.ui.mvi.CollectEffects
import com.calculator.vault.feature.recovery.R
import com.calculator.vault.feature.recovery.presentation.components.OptionTrailing
import com.calculator.vault.feature.recovery.presentation.components.RecoveryOptionCard
import com.calculator.vault.feature.recovery.presentation.components.textRes
import com.calculator.vault.feature.recovery.presentation.mvi.RecoveryMethodEffect
import com.calculator.vault.feature.recovery.presentation.mvi.RecoveryMethodIntent
import com.calculator.vault.feature.recovery.presentation.mvi.RecoveryMethodState
import com.calculator.vault.feature.recovery.presentation.mvi.RecoveryMethodViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun RecoveryMethodRoute(
    fromSettings: Boolean,
    onOpenPattern: () -> Unit,
    onOpenEmail: () -> Unit,
    onOpenSecurityQuestion: () -> Unit,
    onFinish: () -> Unit,
    viewModel: RecoveryMethodViewModel = koinViewModel(key = "recovery-$fromSettings") { parametersOf(fromSettings) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val prompter = rememberBiometricPrompter()
    val snackbar = remember { SnackbarHostState() }
    val verifyTitle = stringResource(R.string.recovery_verify_fingerprint)
    val cancel = stringResource(R.string.recovery_cancel)
    val unavailable = stringResource(R.string.recovery_fingerprint_unavailable)
    viewModel.effects.CollectEffects { effect ->
        when (effect) {
            is RecoveryMethodEffect.VerifyFingerprint -> prompter.authenticate(verifyTitle, null, cancel, effect.cipher) { result ->
                if (result is BiometricResult.Success) viewModel.onIntent(RecoveryMethodIntent.FingerprintVerified(result.cipher))
            }
            RecoveryMethodEffect.FingerprintUnavailable -> snackbar.showToast(unavailable, ToastKind.Error)
            RecoveryMethodEffect.OpenPattern -> onOpenPattern()
            RecoveryMethodEffect.OpenEmail -> onOpenEmail()
            RecoveryMethodEffect.OpenSecurityQuestion -> onOpenSecurityQuestion()
            RecoveryMethodEffect.Finish -> onFinish()
        }
    }
    BackHandler { viewModel.onIntent(RecoveryMethodIntent.Back) }
    RecoveryMethodScreen(state, prompter.isAvailable() || state.options.fingerprintEnabled, viewModel::onIntent, snackbar)
}

@Composable
fun RecoveryMethodScreen(
    state: RecoveryMethodState,
    fingerprintSupported: Boolean,
    onIntent: (RecoveryMethodIntent) -> Unit,
    snackbar: SnackbarHostState = remember { SnackbarHostState() },
) {
    VaultScreen(
        snackbarHostState = snackbar,
        topBar = {
            VaultTopBar(
                title = stringResource(R.string.recovery_title),
                navigationIcon = if (state.fromSettings) NavigationIcon.Back else null,
                onNavigationClick = { onIntent(RecoveryMethodIntent.Back) },
            ) {
                if (!state.fromSettings) {
                    VaultTextButton(stringResource(R.string.recovery_skip), { onIntent(RecoveryMethodIntent.Skip) }, color = VaultTheme.colors.skip)
                }
            }
        },
        bottomVisible = !state.fromSettings && state.options.hasAnyMethod,
        bottomBar = { VaultButton(stringResource(R.string.recovery_continue), { onIntent(RecoveryMethodIntent.Skip) }) },
    ) {
        Column(
            Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = 560.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.recovery_headline), style = VaultTheme.typography.headlineLarge, color = VaultTheme.scheme.onBackground)
            Spacer(Modifier.height(20.dp))
            val options = state.options
            if (fingerprintSupported) {
                RecoveryOptionCard(
                    icon = VaultIcons.FingerprintLine,
                    title = stringResource(R.string.recovery_add_fingerprint),
                    trailing = OptionTrailing.Toggle(options.fingerprintEnabled) { onIntent(RecoveryMethodIntent.FingerprintToggled(it)) },
                )
            }
            RecoveryOptionCard(
                icon = VaultIcons.PatternSmall,
                title = stringResource(if (options.hasPattern) R.string.recovery_change_pattern else R.string.recovery_add_pattern),
                trailing = OptionTrailing.Chevron(options.hasPattern),
                onClick = { onIntent(RecoveryMethodIntent.Pattern) },
            )
            RecoveryOptionCard(
                icon = VaultIcons.Mail,
                title = stringResource(R.string.recovery_add_email),
                subtitle = options.email,
                trailing = OptionTrailing.Chevron(options.email != null),
                onClick = { onIntent(RecoveryMethodIntent.Email) },
            )
            RecoveryOptionCard(
                icon = VaultIcons.HelpCircle,
                title = stringResource(R.string.recovery_add_question),
                subtitle = options.securityQuestion?.let { stringResource(it.textRes()) },
                trailing = OptionTrailing.Chevron(options.securityQuestion != null),
                onClick = { onIntent(RecoveryMethodIntent.SecurityQuestion) },
            )
            Text(
                stringResource(R.string.recovery_reinstall_note),
                style = VaultTheme.typography.bodySmall,
                color = VaultTheme.scheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            )
        }
    }
}

@ScreenPreviews
@Composable
private fun RecoveryMethodPreview() = VaultPreview {
    RecoveryMethodScreen(
        RecoveryMethodState(options = RecoveryOptions(fingerprintEnabled = true, email = "me@mail.com", securityQuestion = SecurityQuestion.FIRST_PET)),
        fingerprintSupported = true,
        onIntent = {},
    )
}
