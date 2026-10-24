package com.calculator.vault.feature.settings.presentation.ui

import android.Manifest
import android.app.Activity
import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calculator.vault.core.common.VaultConstants
import com.calculator.vault.core.common.session.LockGuard
import com.calculator.vault.core.designsystem.component.InfoBanner
import com.calculator.vault.core.designsystem.component.RowTrailing
import com.calculator.vault.core.designsystem.component.SectionLabel
import com.calculator.vault.core.designsystem.component.SettingsRow
import com.calculator.vault.core.designsystem.component.ToastKind
import com.calculator.vault.core.designsystem.component.VaultCardColumn
import com.calculator.vault.core.designsystem.component.VaultSnackbarHost
import com.calculator.vault.core.designsystem.component.VaultTopBar
import com.calculator.vault.core.designsystem.component.showToast
import com.calculator.vault.core.designsystem.icon.VaultIcons
import com.calculator.vault.core.designsystem.preview.ScreenPreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultTheme
import com.calculator.vault.core.model.AppLockSettings
import com.calculator.vault.core.model.BackupSettings
import com.calculator.vault.core.model.RecoveryOptions
import com.calculator.vault.core.model.ThemeMode
import com.calculator.vault.core.model.UserSettings
import com.calculator.vault.core.ui.biometric.BiometricResult
import com.calculator.vault.core.ui.biometric.rememberBiometricPrompter
import com.calculator.vault.core.ui.component.ResetCodeBanner
import com.calculator.vault.core.ui.component.labelRes
import com.calculator.vault.core.ui.mvi.CollectEffects
import com.calculator.vault.core.ui.util.ExternalIntents
import com.calculator.vault.feature.settings.R
import com.calculator.vault.feature.settings.domain.usecase.SettingsOverview
import com.calculator.vault.feature.settings.presentation.components.UninstallProtectionSheet
import com.calculator.vault.feature.settings.presentation.mvi.ExternalAction
import com.calculator.vault.feature.settings.presentation.mvi.SettingsDestination
import com.calculator.vault.feature.settings.presentation.mvi.SettingsEffect
import com.calculator.vault.feature.settings.presentation.mvi.SettingsIntent
import com.calculator.vault.feature.settings.presentation.mvi.SettingsState
import com.calculator.vault.feature.settings.presentation.mvi.SettingsViewModel
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.ktx.launchReview
import com.google.android.play.core.ktx.requestReview
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

/** Settings tab content, hosted inside the vault shell next to Home. */
@Composable
fun SettingsTab(
    contentPadding: PaddingValues,
    versionLabel: String,
    onNavigate: (SettingsDestination) -> Unit,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = LocalActivity.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val prompter = rememberBiometricPrompter()
    val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        viewModel.onIntent(SettingsIntent.CameraPermission(it))
    }
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    val deviceAdmin = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        viewModel.onIntent(SettingsIntent.Resumed)
    }
    val verifyTitle = stringResource(R.string.settings_verify_fingerprint)
    val cancel = stringResource(R.string.settings_cancel)
    val fingerprintUnavailable = stringResource(R.string.settings_fingerprint_unavailable)
    val adminExplanation = stringResource(R.string.settings_uninstall_sheet_message)
    val protectionOff = stringResource(R.string.settings_uninstall_off)
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.onIntent(SettingsIntent.Resumed) }
    viewModel.effects.CollectEffects { effect ->
        when (effect) {
            is SettingsEffect.Navigate -> onNavigate(effect.destination)
            is SettingsEffect.RunExternal -> runExternal(effect.action, context, activity) { scope.launch { it() } }
            is SettingsEffect.PromptFingerprint -> prompter.authenticate(verifyTitle, null, cancel, effect.cipher) { result ->
                if (result is BiometricResult.Success) viewModel.onIntent(SettingsIntent.FingerprintVerified(result.cipher))
            }
            SettingsEffect.FingerprintUnavailable -> snackbar.showToast(fingerprintUnavailable, ToastKind.Error)
            SettingsEffect.RequestCamera -> {
                LockGuard.allowExternalLaunch()
                cameraPermission.launch(Manifest.permission.CAMERA)
            }
            SettingsEffect.RequestNotifications -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                LockGuard.allowExternalLaunch()
                notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            SettingsEffect.LaunchDeviceAdmin -> {
                LockGuard.allowExternalLaunch()
                deviceAdmin.launch(com.calculator.vault.feature.settings.data.UninstallProtectionImpl(context).activationIntent(adminExplanation))
            }
            SettingsEffect.UninstallProtectionOff -> snackbar.showToast(protectionOff, ToastKind.Info)
        }
    }
    SettingsContent(state, viewModel::onIntent, contentPadding, versionLabel, prompter.isAvailable())
    if (state.showUninstallSheet) {
        UninstallProtectionSheet(
            onActivate = { viewModel.onIntent(SettingsIntent.ActivateUninstallProtection) },
            onDismiss = { viewModel.onIntent(SettingsIntent.DismissUninstallSheet) },
        )
    }
    VaultSnackbarHost(snackbar)
}

private fun runExternal(action: ExternalAction, context: android.content.Context, activity: Activity?, launch: (suspend () -> Unit) -> Unit) {
    val storeUrl = VaultConstants.PLAY_STORE_URL + context.packageName.removeSuffix(".debug")
    when (action) {
        ExternalAction.FEEDBACK -> ExternalIntents.sendEmail(context, VaultConstants.SUPPORT_EMAIL, context.getString(R.string.settings_feedback_subject))
        ExternalAction.SHARE -> ExternalIntents.shareText(context, context.getString(R.string.settings_share_text, storeUrl))
        ExternalAction.PRIVACY -> ExternalIntents.openUrl(context, VaultConstants.PRIVACY_POLICY_URL)
        ExternalAction.TERMS -> ExternalIntents.openUrl(context, VaultConstants.TERMS_URL)
        ExternalAction.RATE -> launch {
            val host = activity
            val reviewed = host != null && runCatching {
                val manager = ReviewManagerFactory.create(host)
                LockGuard.allowExternalLaunch()
                manager.launchReview(host, manager.requestReview())
            }.isSuccess
            if (!reviewed) ExternalIntents.openUrl(context, storeUrl)
        }
    }
}

@Composable
fun SettingsContent(
    state: SettingsState,
    onIntent: (SettingsIntent) -> Unit,
    contentPadding: PaddingValues,
    versionLabel: String,
    fingerprintSupported: Boolean,
) {
    val overview = state.overview ?: return
    val s = overview.settings
    Column(Modifier.fillMaxSize()) {
        VaultTopBar(stringResource(R.string.settings_title), navigationIcon = null)
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val twoColumns = maxWidth >= 840.dp
            Column(
                Modifier
                    .align(Alignment.TopCenter)
                    .widthIn(max = if (twoColumns) 1100.dp else 640.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(contentPadding),
            ) {
                // Once the backup promo is gone its slot keeps the reset-code reminder for good.
                AnimatedContent(
                    targetState = overview.backup.accountEmail == null && !s.cloudBannerDismissed,
                    transitionSpec = { (fadeIn() + expandVertically()) togetherWith (fadeOut() + shrinkVertically()) },
                    label = "settingsBanner",
                ) { showBackup ->
                    if (showBackup) {
                        InfoBanner(
                            title = stringResource(R.string.settings_banner_backup_title),
                            message = stringResource(R.string.settings_banner_backup_message),
                            icon = VaultIcons.Cloud,
                            onClick = { onIntent(SettingsIntent.Open(SettingsDestination.CLOUD_BACKUP)) },
                            onDismiss = { onIntent(SettingsIntent.DismissCloudBanner) },
                            modifier = Modifier.padding(bottom = 16.dp),
                        )
                    } else {
                        ResetCodeBanner(onDismiss = null, modifier = Modifier.padding(bottom = 16.dp))
                    }
                }
                if (twoColumns) {
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        Column(Modifier.weight(1f)) { PrimarySections(state, overview, onIntent, fingerprintSupported) }
                        Column(Modifier.weight(1f)) { SecondarySections(state, overview, onIntent) }
                    }
                } else {
                    PrimarySections(state, overview, onIntent, fingerprintSupported)
                    SecondarySections(state, overview, onIntent)
                }
                Text(
                    versionLabel,
                    style = VaultTheme.typography.bodyMedium,
                    color = VaultTheme.scheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                )
            }
        }
    }
}

@Composable
private fun PrimarySections(state: SettingsState, o: SettingsOverview, onIntent: (SettingsIntent) -> Unit, fingerprintSupported: Boolean) {
    val s = o.settings
    SectionLabel(stringResource(R.string.settings_section_account))
    VaultCardColumn {
        SettingsRow(
            stringResource(R.string.settings_login),
            subtitle = o.backup.accountEmail ?: stringResource(R.string.settings_login_desc),
            icon = VaultIcons.User,
            onClick = { onIntent(SettingsIntent.Open(SettingsDestination.ACCOUNT)) },
        )
    }
    Section(stringResource(R.string.settings_section_appearance))
    VaultCardColumn {
        SettingsRow(
            stringResource(R.string.settings_theme),
            icon = VaultIcons.Palette,
            trailing = RowTrailing.Value(stringResource(s.themeMode.labelRes())),
            showDivider = true,
            onClick = { onIntent(SettingsIntent.Open(SettingsDestination.THEME)) },
        )
        SettingsRow(
            stringResource(R.string.settings_app_icon),
            icon = VaultIcons.AppWindow,
            trailing = RowTrailing.Value(stringResource(s.appIcon.labelRes())),
            showDivider = true,
            onClick = { onIntent(SettingsIntent.Open(SettingsDestination.APP_ICON)) },
        )
        SettingsRow(
            stringResource(R.string.settings_language),
            icon = VaultIcons.Globe,
            trailing = RowTrailing.Value(languageLabel(s.languageTag)),
            onClick = { onIntent(SettingsIntent.Open(SettingsDestination.LANGUAGE)) },
        )
    }
    Section(stringResource(R.string.settings_section_security))
    VaultCardColumn {
        SettingsRow(
            stringResource(R.string.settings_app_lock),
            icon = VaultIcons.LockKeyhole,
            trailing = RowTrailing.Value(stringResource(if (o.appLock.enabled) R.string.settings_on else R.string.settings_off)),
            showDivider = true,
            onClick = { onIntent(SettingsIntent.Open(SettingsDestination.APP_LOCK)) },
        )
        SettingsRow(stringResource(R.string.settings_change_pin), icon = VaultIcons.Key, showDivider = true, onClick = { onIntent(SettingsIntent.Open(SettingsDestination.CHANGE_PIN)) })
        SettingsRow(stringResource(R.string.settings_change_app_lock), icon = VaultIcons.Pattern, showDivider = true, onClick = { onIntent(SettingsIntent.Open(SettingsDestination.APP_LOCK_SECURITY)) })
        if (fingerprintSupported || o.fingerprintUnlock) {
            SettingsRow(
                stringResource(R.string.settings_fingerprint),
                subtitle = stringResource(R.string.settings_fingerprint_desc),
                icon = VaultIcons.Fingerprint,
                trailing = RowTrailing.Toggle(o.fingerprintUnlock) { onIntent(SettingsIntent.Fingerprint(it)) },
                showDivider = true,
            )
        }
        SettingsRow(
            stringResource(R.string.settings_recovery),
            icon = VaultIcons.HelpCircle,
            trailing = RowTrailing.Value(stringResource(if (o.recovery.hasAnyMethod) R.string.settings_on else R.string.settings_off)),
            onClick = { onIntent(SettingsIntent.Open(SettingsDestination.RECOVERY)) },
        )
    }
    Section(stringResource(R.string.settings_section_privacy))
    VaultCardColumn {
        SettingsRow(stringResource(R.string.settings_intruder), icon = VaultIcons.ShieldCheckAlt, showDivider = true, onClick = { onIntent(SettingsIntent.Open(SettingsDestination.INTRUDER)) })
        SettingsRow(
            stringResource(R.string.settings_intruder_selfie),
            subtitle = pluralStringResource(R.plurals.settings_intruder_selfie_desc, s.intruderAttempts, s.intruderAttempts),
            icon = VaultIcons.ScanFace,
            trailing = RowTrailing.Toggle(s.intruderSelfieEnabled) { onIntent(SettingsIntent.IntruderSelfie(it)) },
            showDivider = true,
        )
        SettingsRow(
            stringResource(R.string.settings_panic),
            subtitle = stringResource(R.string.settings_panic_desc),
            icon = VaultIcons.Siren,
            trailing = RowTrailing.Toggle(s.panicModeEnabled) { onIntent(SettingsIntent.Panic(it)) },
            showDivider = true,
        )
        SettingsRow(
            stringResource(R.string.settings_screenshots),
            subtitle = stringResource(R.string.settings_screenshots_desc),
            icon = VaultIcons.ScreenshotBlock,
            trailing = RowTrailing.Toggle(s.preventScreenshots) { onIntent(SettingsIntent.PreventScreenshots(it)) },
            showDivider = true,
        )
        SettingsRow(
            stringResource(R.string.settings_uninstall),
            subtitle = stringResource(R.string.settings_uninstall_desc),
            icon = VaultIcons.PackageX,
            trailing = RowTrailing.Toggle(state.uninstallProtected) { onIntent(SettingsIntent.PreventUninstall(it)) },
        )
    }
}

@Composable
private fun SecondarySections(state: SettingsState, o: SettingsOverview, onIntent: (SettingsIntent) -> Unit) {
    Section(stringResource(R.string.settings_section_storage))
    VaultCardColumn {
        SettingsRow(
            stringResource(R.string.settings_cloud_backup),
            icon = VaultIcons.CloudOutline,
            trailing = RowTrailing.Value(stringResource(if (o.backup.autoBackup) R.string.settings_auto else R.string.settings_off)),
            showDivider = true,
            onClick = { onIntent(SettingsIntent.Open(SettingsDestination.CLOUD_BACKUP)) },
        )
        SettingsRow(
            stringResource(R.string.settings_trash),
            subtitle = stringResource(R.string.settings_trash_desc),
            icon = VaultIcons.Trash,
            trailing = RowTrailing.Value(o.trashCount.toString()),
            onClick = { onIntent(SettingsIntent.Open(SettingsDestination.TRASH)) },
        )
    }
    Section(stringResource(R.string.settings_section_general))
    VaultCardColumn {
        SettingsRow(
            stringResource(R.string.settings_notifications),
            icon = VaultIcons.Bell,
            trailing = RowTrailing.Toggle(o.settings.notificationsEnabled) { onIntent(SettingsIntent.Notifications(it)) },
            showDivider = true,
        )
        SettingsRow(stringResource(R.string.settings_feedback), icon = VaultIcons.Feedback, showDivider = true, onClick = { onIntent(SettingsIntent.External(ExternalAction.FEEDBACK)) })
        SettingsRow(stringResource(R.string.settings_rate), icon = VaultIcons.Star, showDivider = true, onClick = { onIntent(SettingsIntent.External(ExternalAction.RATE)) })
        SettingsRow(stringResource(R.string.settings_share), icon = VaultIcons.Share, showDivider = true, onClick = { onIntent(SettingsIntent.External(ExternalAction.SHARE)) })
        SettingsRow(stringResource(R.string.settings_privacy), icon = VaultIcons.Document, showDivider = true, onClick = { onIntent(SettingsIntent.External(ExternalAction.PRIVACY)) })
        SettingsRow(stringResource(R.string.settings_terms), icon = VaultIcons.Terms, onClick = { onIntent(SettingsIntent.External(ExternalAction.TERMS)) })
    }
}

@Composable
private fun Section(title: String) = SectionLabel(title, Modifier.padding(top = 20.dp))

private fun ThemeMode.labelRes(): Int = when (this) {
    ThemeMode.SYSTEM -> R.string.settings_theme_system
    ThemeMode.LIGHT -> R.string.settings_theme_light
    ThemeMode.DARK -> R.string.settings_theme_dark
}

@Composable
private fun languageLabel(tag: String?): String {
    if (tag == null) return stringResource(R.string.settings_theme_system)
    val locale = Locale.forLanguageTag(tag)
    return locale.getDisplayLanguage(locale).replaceFirstChar { it.titlecase(locale) }
}

@ScreenPreviews
@Composable
private fun SettingsPreview() = VaultPreview {
    SettingsContent(
        SettingsState(
            SettingsOverview(UserSettings(themeMode = ThemeMode.LIGHT), AppLockSettings(enabled = true), BackupSettings(), RecoveryOptions(), true, 3),
        ),
        onIntent = {},
        contentPadding = PaddingValues(bottom = 140.dp),
        versionLabel = "Calculator Vault 1.0.0 · Build 1",
        fingerprintSupported = true,
    )
}
