package com.calculator.vault.feature.recovery.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calculator.vault.core.designsystem.component.VaultButton
import com.calculator.vault.core.designsystem.component.VaultTextButton
import com.calculator.vault.core.designsystem.component.VaultTextField
import com.calculator.vault.core.designsystem.component.VaultTopBar
import com.calculator.vault.core.designsystem.preview.ScreenPreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultTheme
import com.calculator.vault.core.navigation.OtpPurpose
import com.calculator.vault.core.ui.component.VaultScreen
import com.calculator.vault.core.ui.mvi.CollectEffects
import com.calculator.vault.core.ui.util.formatDuration
import com.calculator.vault.feature.recovery.R
import com.calculator.vault.feature.recovery.presentation.components.OtpInput
import com.calculator.vault.feature.recovery.presentation.mvi.EmailError
import com.calculator.vault.feature.recovery.presentation.mvi.EmailOtpEffect
import com.calculator.vault.feature.recovery.presentation.mvi.EmailOtpIntent
import com.calculator.vault.feature.recovery.presentation.mvi.EmailOtpState
import com.calculator.vault.feature.recovery.presentation.mvi.EmailOtpViewModel
import com.calculator.vault.feature.recovery.presentation.mvi.RecoveryEmailEffect
import com.calculator.vault.feature.recovery.presentation.mvi.RecoveryEmailIntent
import com.calculator.vault.feature.recovery.presentation.mvi.RecoveryEmailState
import com.calculator.vault.feature.recovery.presentation.mvi.RecoveryEmailViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun emailErrorText(error: EmailError?): String? = when (error) {
    EmailError.OFFLINE -> stringResource(R.string.recovery_error_offline)
    EmailError.SEND_FAILED -> stringResource(R.string.recovery_error_send)
    EmailError.WRONG_CODE -> stringResource(R.string.recovery_error_wrong_code)
    EmailError.VERIFY_FAILED -> stringResource(R.string.recovery_error_verify)
    null -> null
}

@Composable
fun RecoveryEmailRoute(onCodeSent: (String) -> Unit, onClose: () -> Unit, viewModel: RecoveryEmailViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    viewModel.effects.CollectEffects { effect ->
        when (effect) {
            is RecoveryEmailEffect.CodeSent -> onCodeSent(effect.email)
            RecoveryEmailEffect.Close -> onClose()
        }
    }
    RecoveryEmailScreen(state, viewModel::onIntent)
}

@Composable
fun RecoveryEmailScreen(state: RecoveryEmailState, onIntent: (RecoveryEmailIntent) -> Unit) {
    VaultScreen(
        topBar = { VaultTopBar(stringResource(R.string.recovery_title), onNavigationClick = { onIntent(RecoveryEmailIntent.Back) }) },
        bottomBar = {
            VaultButton(
                stringResource(R.string.recovery_continue),
                { onIntent(RecoveryEmailIntent.Continue) },
                enabled = state.isValid,
                loading = state.sending,
            )
        },
    ) {
        Column(
            Modifier.align(Alignment.TopCenter).widthIn(max = 560.dp).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.recovery_email_headline), style = VaultTheme.typography.headlineLarge, color = VaultTheme.scheme.onBackground)
            Spacer(Modifier.height(20.dp))
            VaultTextField(
                value = state.email,
                onValueChange = { onIntent(RecoveryEmailIntent.EmailChanged(it)) },
                placeholder = stringResource(R.string.recovery_email_placeholder),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onIntent(RecoveryEmailIntent.Continue) }),
                isError = state.error != null,
                errorText = emailErrorText(state.error),
            )
            Text(stringResource(R.string.recovery_email_note), style = VaultTheme.typography.bodySmall, color = VaultTheme.scheme.onSurfaceVariant)
        }
    }
}

@Composable
fun EmailOtpRoute(
    email: String,
    purpose: OtpPurpose,
    onEmailSaved: () -> Unit,
    onVaultUnlocked: () -> Unit,
    onClose: () -> Unit,
    viewModel: EmailOtpViewModel = koinViewModel(key = "otp-$email-$purpose") { parametersOf(email, purpose) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    viewModel.effects.CollectEffects { effect ->
        when (effect) {
            EmailOtpEffect.EmailSaved -> onEmailSaved()
            EmailOtpEffect.VaultUnlocked -> onVaultUnlocked()
            EmailOtpEffect.Close -> onClose()
        }
    }
    EmailOtpScreen(state, viewModel::onIntent)
}

@Composable
fun EmailOtpScreen(state: EmailOtpState, onIntent: (EmailOtpIntent) -> Unit) {
    VaultScreen(
        topBar = { VaultTopBar(stringResource(R.string.recovery_otp_title), onNavigationClick = { onIntent(EmailOtpIntent.Back) }) },
        bottomBar = {
            VaultButton(stringResource(R.string.recovery_verify), { onIntent(EmailOtpIntent.Verify) }, enabled = state.code.length == EmailOtpState.CODE_LENGTH, loading = state.verifying)
        },
    ) {
        Column(
            Modifier.align(Alignment.TopCenter).widthIn(max = 560.dp).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.recovery_otp_headline), style = VaultTheme.typography.headlineLarge, color = VaultTheme.scheme.onBackground)
            Text(stringResource(R.string.recovery_otp_message, state.email), style = VaultTheme.typography.bodyMedium, color = VaultTheme.scheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            OtpInput(state.code, { onIntent(EmailOtpIntent.CodeChanged(it)) }, EmailOtpState.CODE_LENGTH, isError = state.error != null, modifier = Modifier.fillMaxWidth())
            emailErrorText(state.error)?.let { Text(it, style = VaultTheme.typography.bodySmall, color = VaultTheme.scheme.error) }
            if (state.resendIn > 0) {
                Text(
                    stringResource(R.string.recovery_resend_in, formatDuration(state.resendIn * 1000L)),
                    style = VaultTheme.typography.bodyMedium,
                    color = VaultTheme.scheme.onSurfaceVariant,
                )
            } else {
                VaultTextButton(stringResource(R.string.recovery_resend), { onIntent(EmailOtpIntent.Resend) })
            }
        }
    }
}

@ScreenPreviews
@Composable
private fun RecoveryEmailPreview() = VaultPreview {
    RecoveryEmailScreen(RecoveryEmailState(email = "example@gmail.com", isValid = true), onIntent = {})
}

@ScreenPreviews
@Composable
private fun EmailOtpPreview() = VaultPreview {
    EmailOtpScreen(EmailOtpState("example@gmail.com", OtpPurpose.VERIFY_RECOVERY_EMAIL, code = "123", resendIn = 42), onIntent = {})
}
