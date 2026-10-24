package com.calculator.vault.feature.applock.presentation.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calculator.vault.core.designsystem.component.IconTile
import com.calculator.vault.core.designsystem.component.RowTrailing
import com.calculator.vault.core.designsystem.component.SectionLabel
import com.calculator.vault.core.designsystem.component.SettingsRow
import com.calculator.vault.core.designsystem.component.VaultCardColumn
import com.calculator.vault.core.designsystem.component.VaultTopBar
import com.calculator.vault.core.designsystem.icon.VaultIcons
import com.calculator.vault.core.designsystem.preview.ScreenPreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.model.AppLockAuthMethod
import com.calculator.vault.core.model.AppLockSettings
import com.calculator.vault.core.model.LockAfter
import com.calculator.vault.core.ui.biometric.rememberBiometricPrompter
import com.calculator.vault.core.ui.component.VaultScreen
import com.calculator.vault.core.ui.mvi.CollectEffects
import com.calculator.vault.feature.applock.R
import com.calculator.vault.feature.applock.presentation.mvi.AppLockSettingsEffect
import com.calculator.vault.feature.applock.presentation.mvi.AppLockSettingsIntent
import com.calculator.vault.feature.applock.presentation.mvi.AppLockSettingsState
import com.calculator.vault.feature.applock.presentation.mvi.AppLockSettingsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppLockSettingsRoute(onSetupPattern: () -> Unit, onClose: () -> Unit, viewModel: AppLockSettingsViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val prompter = rememberBiometricPrompter()
    viewModel.effects.CollectEffects { effect ->
        when (effect) {
            AppLockSettingsEffect.OpenPatternSetup -> onSetupPattern()
            AppLockSettingsEffect.Close -> onClose()
        }
    }
    AppLockSettingsScreen(state.copy(fingerprintAvailable = prompter.isAvailable()), viewModel::onIntent)
}

@Composable
fun AppLockSettingsScreen(state: AppLockSettingsState, onIntent: (AppLockSettingsIntent) -> Unit) {
    val s = state.settings
    val contentAlpha by animateFloatAsState(if (s.enabled) 1f else 0.45f, label = "appLockAlpha")
    VaultScreen(topBar = { VaultTopBar(stringResource(R.string.applock_settings_title), onNavigationClick = { onIntent(AppLockSettingsIntent.Back) }) }) {
        Column(
            Modifier.align(Alignment.TopCenter).widthIn(max = 560.dp).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            VaultCardColumn {
                Row(Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconTile(VaultIcons.LockFilled)
                    SettingsRow(
                        title = stringResource(R.string.applock_settings_title),
                        subtitle = stringResource(R.string.applock_settings_desc),
                        trailing = RowTrailing.Toggle(s.enabled) { onIntent(AppLockSettingsIntent.Enabled(it)) },
                    )
                }
            }
            Column(Modifier.alpha(contentAlpha), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionLabel(stringResource(R.string.applock_lock_after), Modifier.padding(top = 12.dp))
                VaultCardColumn {
                    LockAfter.entries.forEachIndexed { index, value ->
                        SettingsRow(
                            title = stringResource(value.labelRes()),
                            trailing = RowTrailing.Radio(s.lockAfter == value),
                            enabled = s.enabled,
                            showDivider = index < LockAfter.entries.lastIndex,
                            onClick = { onIntent(AppLockSettingsIntent.LockAfterChanged(value)) },
                        )
                    }
                }
                SectionLabel(stringResource(R.string.applock_authentication), Modifier.padding(top = 12.dp))
                VaultCardColumn {
                    SettingsRow(
                        title = stringResource(R.string.applock_method_pin),
                        icon = VaultIcons.Pin,
                        trailing = RowTrailing.Check(s.authMethod == AppLockAuthMethod.PIN, showChevron = false),
                        enabled = s.enabled,
                        showDivider = true,
                        onClick = { onIntent(AppLockSettingsIntent.Method(AppLockAuthMethod.PIN)) },
                    )
                    if (state.fingerprintAvailable) {
                        SettingsRow(
                            title = stringResource(R.string.applock_method_fingerprint),
                            icon = VaultIcons.Fingerprint,
                            trailing = RowTrailing.Check(s.authMethod == AppLockAuthMethod.FINGERPRINT, showChevron = false),
                            enabled = s.enabled,
                            showDivider = true,
                            onClick = { onIntent(AppLockSettingsIntent.Method(AppLockAuthMethod.FINGERPRINT)) },
                        )
                    }
                    SettingsRow(
                        title = stringResource(R.string.applock_method_pattern),
                        subtitle = if (s.hasPattern) stringResource(R.string.applock_change_pattern) else stringResource(R.string.applock_set_pattern),
                        icon = VaultIcons.Pattern,
                        trailing = RowTrailing.Check(s.authMethod == AppLockAuthMethod.PATTERN, showChevron = true),
                        enabled = s.enabled,
                        onClick = {
                            if (s.authMethod == AppLockAuthMethod.PATTERN) onIntent(AppLockSettingsIntent.SetupPattern)
                            else onIntent(AppLockSettingsIntent.Method(AppLockAuthMethod.PATTERN))
                        },
                    )
                }
            }
        }
    }
}

private fun LockAfter.labelRes(): Int = when (this) {
    LockAfter.IMMEDIATELY -> R.string.applock_immediately
    LockAfter.SECONDS_30 -> R.string.applock_after_30s
    LockAfter.MINUTE_1 -> R.string.applock_after_1m
    LockAfter.MINUTES_5 -> R.string.applock_after_5m
}

@ScreenPreviews
@Composable
private fun AppLockSettingsPreview() = VaultPreview { AppLockSettingsScreen(AppLockSettingsState(AppLockSettings(enabled = true)), onIntent = {}) }

@ScreenPreviews
@Composable
private fun AppLockSettingsDisabledPreview() = VaultPreview { AppLockSettingsScreen(AppLockSettingsState(AppLockSettings(enabled = false)), onIntent = {}) }
