package com.calculator.vault.feature.recovery.presentation.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calculator.vault.core.designsystem.component.LoadingContent
import com.calculator.vault.core.designsystem.component.PatternLock
import com.calculator.vault.core.designsystem.component.VaultButton
import com.calculator.vault.core.designsystem.component.VaultCard
import com.calculator.vault.core.designsystem.component.VaultTextField
import com.calculator.vault.core.designsystem.component.VaultTopBar
import com.calculator.vault.core.designsystem.preview.ScreenPreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultTheme
import com.calculator.vault.core.model.RecoveryMethod
import com.calculator.vault.core.model.SecurityQuestion
import com.calculator.vault.core.ui.biometric.BiometricResult
import com.calculator.vault.core.ui.biometric.rememberBiometricPrompter
import com.calculator.vault.core.ui.component.VaultScreen
import com.calculator.vault.core.ui.component.actionRes
import com.calculator.vault.core.ui.component.iconRes
import com.calculator.vault.core.ui.mvi.CollectEffects
import com.calculator.vault.feature.recovery.R
import com.calculator.vault.feature.recovery.presentation.components.OptionTrailing
import com.calculator.vault.feature.recovery.presentation.components.RecoveryOptionCard
import com.calculator.vault.feature.recovery.presentation.components.textRes
import com.calculator.vault.feature.recovery.presentation.mvi.ForgotPinEffect
import com.calculator.vault.feature.recovery.presentation.mvi.ForgotPinIntent
import com.calculator.vault.feature.recovery.presentation.mvi.ForgotPinState
import com.calculator.vault.feature.recovery.presentation.mvi.ForgotPinViewModel
import org.koin.androidx.compose.koinViewModel

/** @param method the method already picked in the calculator's "Forgot PIN?" dialog, if any. */
@Composable
fun ForgotPinRoute(
    method: RecoveryMethod?,
    onRecovered: () -> Unit,
    onOtpSent: (String) -> Unit,
    onClose: () -> Unit,
    viewModel: ForgotPinViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(method) { viewModel.onIntent(ForgotPinIntent.Start(method)) }
    val prompter = rememberBiometricPrompter()
    val title = stringResource(R.string.recovery_verify_fingerprint)
    val cancel = stringResource(R.string.recovery_cancel)
    viewModel.effects.CollectEffects { effect ->
        when (effect) {
            ForgotPinEffect.Recovered -> onRecovered()
            is ForgotPinEffect.OtpSent -> onOtpSent(effect.email)
            ForgotPinEffect.Close -> onClose()
            is ForgotPinEffect.PromptFingerprint -> prompter.authenticate(title, null, cancel, effect.cipher) { result ->
                if (result is BiometricResult.Success) viewModel.onIntent(ForgotPinIntent.FingerprintVerified(result.cipher))
            }
        }
    }
    BackHandler { viewModel.onIntent(ForgotPinIntent.Back) }
    ForgotPinScreen(state, viewModel::onIntent)
}

@Composable
fun ForgotPinScreen(state: ForgotPinState, onIntent: (ForgotPinIntent) -> Unit) {
    VaultScreen(
        topBar = { VaultTopBar(stringResource(R.string.recovery_forgot_title), onNavigationClick = { onIntent(ForgotPinIntent.Back) }) },
        bottomVisible = state.active == RecoveryMethod.SECURITY_QUESTION,
        bottomBar = {
            VaultButton(stringResource(R.string.recovery_continue), { onIntent(ForgotPinIntent.SubmitAnswer) }, enabled = state.answer.isNotBlank(), loading = state.busy)
        },
    ) {
        if (state.loading) {
            LoadingContent()
            return@VaultScreen
        }
        AnimatedContent(
            targetState = state.active,
            transitionSpec = { (slideInHorizontally { it / 4 } + fadeIn()) togetherWith fadeOut() },
            label = "forgotMethod",
            modifier = Modifier.align(Alignment.TopCenter),
        ) { active ->
            Column(
                Modifier.widthIn(max = 560.dp).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                when (active) {
                    null -> MethodList(state, onIntent)
                    RecoveryMethod.PATTERN -> {
                        Text(stringResource(R.string.recovery_draw_to_unlock), style = VaultTheme.typography.headlineMedium, color = VaultTheme.scheme.onBackground)
                        Text(
                            stringResource(if (state.patternError) R.string.recovery_pattern_wrong else R.string.recovery_pattern_hint),
                            style = VaultTheme.typography.bodyMedium,
                            color = if (state.patternError) VaultTheme.scheme.error else VaultTheme.scheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(24.dp))
                        PatternLock(
                            onPatternComplete = { onIntent(ForgotPinIntent.PatternDrawn(it)) },
                            isError = state.patternError,
                            resetKey = state.patternResetKey,
                            modifier = Modifier.widthIn(max = 320.dp),
                        )
                    }
                    RecoveryMethod.SECURITY_QUESTION -> {
                        Text(stringResource(R.string.recovery_question_headline), style = VaultTheme.typography.headlineLarge, color = VaultTheme.scheme.onBackground, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(8.dp))
                        VaultCard(Modifier.fillMaxWidth()) {
                            Text(
                                stringResource((state.question ?: SecurityQuestion.FIRST_PET).textRes()),
                                style = VaultTheme.text.rowTitle,
                                color = VaultTheme.scheme.onSurface,
                                modifier = Modifier.padding(20.dp),
                            )
                        }
                        VaultTextField(
                            value = state.answer,
                            onValueChange = { onIntent(ForgotPinIntent.AnswerChanged(it)) },
                            placeholder = stringResource(R.string.recovery_answer_placeholder),
                            isError = state.wrongAnswer,
                            errorText = stringResource(R.string.recovery_wrong_answer),
                        )
                    }
                    else -> Unit
                }
            }
        }
    }
}

@Composable
private fun MethodList(state: ForgotPinState, onIntent: (ForgotPinIntent) -> Unit) {
    Text(stringResource(R.string.recovery_forgot_headline), style = VaultTheme.typography.headlineLarge, color = VaultTheme.scheme.onBackground, modifier = Modifier.fillMaxWidth())
    if (state.methods.isEmpty()) {
        Text(
            stringResource(R.string.recovery_none_available),
            style = VaultTheme.typography.bodyMedium,
            color = VaultTheme.scheme.onSurfaceVariant,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth(),
        )
        return
    }
    Spacer(Modifier.height(8.dp))
    state.methods.forEach { method ->
        RecoveryOptionCard(
            icon = method.iconRes(),
            title = stringResource(method.actionRes()),
            subtitle = if (method == RecoveryMethod.EMAIL) state.email else null,
            trailing = OptionTrailing.Chevron(false),
            onClick = { onIntent(ForgotPinIntent.Choose(method)) },
        )
    }
    emailErrorText(state.error)?.let { Text(it, style = VaultTheme.typography.bodySmall, color = VaultTheme.scheme.error) }
}

@ScreenPreviews
@Composable
private fun ForgotPinPreview() = VaultPreview {
    ForgotPinScreen(
        ForgotPinState(loading = false, methods = RecoveryMethod.entries, email = "me@mail.com", question = SecurityQuestion.FIRST_PET),
        onIntent = {},
    )
}
