package com.calculator.vault.feature.cloudbackup.presentation.ui

import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calculator.vault.core.common.session.LockGuard
import com.calculator.vault.core.designsystem.component.LoadingContent
import com.calculator.vault.core.designsystem.component.RowTrailing
import com.calculator.vault.core.designsystem.component.SectionLabel
import com.calculator.vault.core.designsystem.component.SettingsRow
import com.calculator.vault.core.designsystem.component.ToastKind
import com.calculator.vault.core.designsystem.component.VaultButton
import com.calculator.vault.core.designsystem.component.VaultButtonStyle
import com.calculator.vault.core.designsystem.component.VaultCardColumn
import com.calculator.vault.core.designsystem.component.VaultDropdownMenu
import com.calculator.vault.core.designsystem.component.VaultMenuItem
import com.calculator.vault.core.designsystem.component.VaultTopBar
import com.calculator.vault.core.designsystem.component.showToast
import com.calculator.vault.core.designsystem.icon.VaultIcons
import com.calculator.vault.core.designsystem.preview.ScreenPreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.model.BackupNetwork
import com.calculator.vault.core.model.BackupSettings
import com.calculator.vault.core.model.BackupStatus
import com.calculator.vault.core.model.StorageQuota
import com.calculator.vault.core.ui.component.OfflineBanner
import com.calculator.vault.core.ui.component.VaultScreen
import com.calculator.vault.core.ui.mvi.CollectEffects
import com.calculator.vault.feature.cloudbackup.R
import com.calculator.vault.feature.cloudbackup.data.GoogleSignIn
import com.calculator.vault.feature.cloudbackup.data.SignInResult
import com.calculator.vault.feature.cloudbackup.presentation.components.BackupStatusCard
import com.calculator.vault.feature.cloudbackup.presentation.mvi.CloudBackupEffect
import com.calculator.vault.feature.cloudbackup.presentation.mvi.CloudBackupIntent
import com.calculator.vault.feature.cloudbackup.presentation.mvi.CloudBackupState
import com.calculator.vault.feature.cloudbackup.presentation.mvi.CloudBackupViewModel
import com.google.android.gms.auth.api.identity.Identity
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import com.calculator.vault.core.ui.R as UiR

@Composable
fun CloudBackupRoute(onClose: () -> Unit, viewModel: CloudBackupViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val activity = LocalActivity.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val consent = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        val auth = result.data?.let { runCatching { Identity.getAuthorizationClient(context).getAuthorizationResultFromIntent(it) }.getOrNull() }
        viewModel.onIntent(CloudBackupIntent.ConsentResult(auth))
    }
    val offline = stringResource(UiR.string.ui_offline)
    val failed = stringResource(R.string.backup_error)
    val notConfigured = stringResource(R.string.backup_not_configured)
    val signedOut = stringResource(R.string.backup_signed_out_toast)
    viewModel.effects.CollectEffects { effect ->
        when (effect) {
            CloudBackupEffect.LaunchSignIn -> scope.launch {
                val host = activity ?: return@launch
                LockGuard.allowExternalLaunch()
                when (val result = GoogleSignIn.signIn(host)) {
                    is SignInResult.Success -> viewModel.onIntent(CloudBackupIntent.SignedIn(result.account))
                    SignInResult.NotConfigured -> snackbar.showToast(notConfigured, ToastKind.Error)
                    is SignInResult.Failed -> snackbar.showToast(failed, ToastKind.Error)
                    SignInResult.Cancelled -> Unit
                }
            }
            is CloudBackupEffect.RequestConsent -> {
                LockGuard.allowExternalLaunch()
                consent.launch(IntentSenderRequest.Builder(effect.intent.intentSender).build())
            }
            CloudBackupEffect.SignedOut -> {
                activity?.let { scope.launch { GoogleSignIn.signOut(it) } }
                snackbar.showToast(signedOut, ToastKind.Info)
            }
            is CloudBackupEffect.Restored -> snackbar.showToast(context.resources.getQuantityString(R.plurals.backup_restored, effect.count, effect.count))
            is CloudBackupEffect.Error -> snackbar.showToast(if (effect.offline) offline else failed, ToastKind.Error)
            CloudBackupEffect.Close -> onClose()
        }
    }
    CloudBackupScreen(state, viewModel::onIntent, snackbar)
}

@Composable
fun CloudBackupScreen(state: CloudBackupState, onIntent: (CloudBackupIntent) -> Unit, snackbar: SnackbarHostState = remember { SnackbarHostState() }) {
    val signedIn = state.settings.accountEmail != null
    VaultScreen(
        snackbarHostState = snackbar,
        topBar = {
            Column {
                VaultTopBar(stringResource(R.string.backup_title), onNavigationClick = { onIntent(CloudBackupIntent.Back) })
                OfflineBanner(!state.online)
            }
        },
        bottomBar = {
            when {
                !signedIn -> VaultButton(stringResource(R.string.backup_sign_in), { onIntent(CloudBackupIntent.SignIn) }, loading = state.busy, leadingIcon = VaultIcons.User)
                state.status is BackupStatus.Interrupted -> VaultButton(stringResource(R.string.backup_retry), { onIntent(CloudBackupIntent.BackupNow) }, style = VaultButtonStyle.Danger, leadingIcon = VaultIcons.Refresh)
                else -> VaultButton(
                    stringResource(R.string.backup_now),
                    { onIntent(CloudBackupIntent.BackupNow) },
                    enabled = state.status !is BackupStatus.Running,
                    leadingIcon = VaultIcons.Refresh,
                )
            }
        },
    ) {
        if (state.loading) {
            LoadingContent()
            return@VaultScreen
        }
        Column(
            Modifier.align(Alignment.TopCenter).widthIn(max = 560.dp).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(top = 8.dp, bottom = 130.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BackupStatusCard(state.status, state.quota)
            SectionLabel(stringResource(R.string.backup_using), Modifier.padding(top = 12.dp))
            VaultCardColumn {
                SettingsRow(
                    stringResource(R.string.backup_wifi_only),
                    trailing = RowTrailing.Radio(state.settings.network == BackupNetwork.WIFI_ONLY),
                    showDivider = true,
                    onClick = { onIntent(CloudBackupIntent.Network(BackupNetwork.WIFI_ONLY)) },
                )
                SettingsRow(
                    stringResource(R.string.backup_wifi_cellular),
                    trailing = RowTrailing.Radio(state.settings.network == BackupNetwork.WIFI_AND_CELLULAR),
                    onClick = { onIntent(CloudBackupIntent.Network(BackupNetwork.WIFI_AND_CELLULAR)) },
                )
            }
            Box(Modifier.padding(top = 12.dp)) {
                VaultCardColumn {
                    SettingsRow(
                        stringResource(R.string.backup_auto),
                        subtitle = stringResource(if (state.settings.network == BackupNetwork.WIFI_ONLY) R.string.backup_auto_wifi else R.string.backup_auto_any),
                        icon = VaultIcons.Sync,
                        enabled = signedIn,
                        trailing = RowTrailing.Toggle(state.settings.autoBackup) { onIntent(CloudBackupIntent.Auto(it)) },
                        showDivider = true,
                    )
                    Box {
                        SettingsRow(
                            stringResource(R.string.backup_account),
                            subtitle = state.settings.accountEmail ?: stringResource(R.string.backup_not_signed_in),
                            icon = VaultIcons.User,
                            onClick = { if (signedIn) onIntent(CloudBackupIntent.OpenAccountMenu) else onIntent(CloudBackupIntent.SignIn) },
                            showDivider = signedIn,
                        )
                        VaultDropdownMenu(state.accountMenuOpen, { onIntent(CloudBackupIntent.CloseAccountMenu) }) {
                            VaultMenuItem(stringResource(R.string.backup_sign_out), { onIntent(CloudBackupIntent.SignOut) }, destructive = true)
                        }
                    }
                    if (signedIn) {
                        SettingsRow(
                            stringResource(if (state.restoring) R.string.backup_restoring else R.string.backup_restore),
                            subtitle = stringResource(R.string.backup_restore_desc),
                            icon = VaultIcons.CloudDownload,
                            enabled = !state.restoring,
                            onClick = { onIntent(CloudBackupIntent.Restore) },
                        )
                    }
                }
            }
        }
    }
}

@ScreenPreviews
@Composable
private fun CloudBackupPreview() = VaultPreview {
    CloudBackupScreen(
        CloudBackupState(
            loading = false,
            settings = BackupSettings(autoBackup = true, accountEmail = "vault@private.app"),
            status = BackupStatus.Idle(System.currentTimeMillis()),
            quota = StorageQuota(28_200_000_000, 64_000_000_000),
        ),
        onIntent = {},
    )
}

@ScreenPreviews
@Composable
private fun CloudBackupSignedOutPreview() = VaultPreview { CloudBackupScreen(CloudBackupState(loading = false, online = false), onIntent = {}) }
