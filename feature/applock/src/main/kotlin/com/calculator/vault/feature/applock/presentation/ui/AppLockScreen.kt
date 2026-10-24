package com.calculator.vault.feature.applock.presentation.ui

import android.content.Context
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calculator.vault.core.common.session.LockGuard
import com.calculator.vault.core.designsystem.component.LoadingContent
import com.calculator.vault.core.designsystem.component.SectionLabel
import com.calculator.vault.core.designsystem.component.VaultIconButton
import com.calculator.vault.core.designsystem.component.VaultTextField
import com.calculator.vault.core.designsystem.component.VaultTopBar
import com.calculator.vault.core.designsystem.component.showToast
import com.calculator.vault.core.designsystem.icon.VaultIcons
import com.calculator.vault.core.designsystem.preview.ScreenPreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.model.InstalledApp
import com.calculator.vault.core.ui.component.VaultScreen
import com.calculator.vault.core.ui.mvi.CollectEffects
import com.calculator.vault.core.ui.util.ExternalIntents
import com.calculator.vault.feature.applock.R
import com.calculator.vault.feature.applock.domain.repository.AppLockPermissions
import com.calculator.vault.feature.applock.presentation.components.AppRow
import com.calculator.vault.feature.applock.presentation.components.LockHint
import com.calculator.vault.feature.applock.presentation.components.PermissionCard
import com.calculator.vault.feature.applock.presentation.mvi.AppLockListEffect
import com.calculator.vault.feature.applock.presentation.mvi.AppLockListIntent
import com.calculator.vault.feature.applock.presentation.mvi.AppLockListState
import com.calculator.vault.feature.applock.presentation.mvi.AppLockListViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppLockRoute(onOpenSettings: () -> Unit, onClose: () -> Unit, viewModel: AppLockListViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val notifications = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    val lockedText = stringResource(R.string.applock_locked_toast)
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.onIntent(AppLockListIntent.Resumed) }
    viewModel.effects.CollectEffects { effect ->
        when (effect) {
            is AppLockListEffect.Locked -> {
                snackbar.showToast(lockedText)
                askNotificationsOnce(context) {
                    LockGuard.allowExternalLaunch()
                    notifications.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            AppLockListEffect.OpenUsageAccess -> ExternalIntents.openUsageAccessSettings(context)
            AppLockListEffect.OpenOverlay -> ExternalIntents.openOverlaySettings(context)
            AppLockListEffect.OpenSettings -> onOpenSettings()
            AppLockListEffect.Close -> onClose()
        }
    }
    BackHandler { viewModel.onIntent(AppLockListIntent.Back) }
    AppLockScreen(state, viewModel::onIntent, snackbar)
}

private fun askNotificationsOnce(context: Context, request: () -> Unit) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    if (androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()) return
    request()
}

@Composable
fun AppLockScreen(state: AppLockListState, onIntent: (AppLockListIntent) -> Unit, snackbar: SnackbarHostState = remember { SnackbarHostState() }) {
    VaultScreen(
        snackbarHostState = snackbar,
        topBar = {
            VaultTopBar(stringResource(R.string.applock_title), onNavigationClick = { onIntent(AppLockListIntent.Back) }) {
                VaultIconButton(VaultIcons.Search, stringResource(R.string.applock_search), { onIntent(AppLockListIntent.ToggleSearch) })
                VaultIconButton(VaultIcons.SettingsOutline, stringResource(R.string.applock_settings), { onIntent(AppLockListIntent.OpenSettings) })
            }
        },
    ) {
        if (state.loading) {
            LoadingContent()
            return@VaultScreen
        }
        LazyColumn(Modifier.align(Alignment.TopCenter).widthIn(max = 720.dp), contentPadding = PaddingValues(bottom = 40.dp)) {
            if (state.searching) {
                item {
                    VaultTextField(state.query, { onIntent(AppLockListIntent.Query(it)) }, stringResource(R.string.applock_search_hint), Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
                }
            }
            permissionItem(state.permissions, onIntent)
            item {
                SectionLabel(stringResource(R.string.applock_all), Modifier.padding(horizontal = 20.dp)) {
                    androidx.compose.material3.Text(
                        state.visibleApps.size.toString(),
                        style = com.calculator.vault.core.designsystem.theme.VaultTheme.typography.labelSmall,
                        color = com.calculator.vault.core.designsystem.theme.VaultTheme.colors.sectionLabel,
                    )
                }
            }
            items(state.visibleApps, key = { it.packageName }) { app ->
                Box(Modifier.animateItem()) {
                    AppRow(app, { onIntent(AppLockListIntent.Toggle(app)) })
                    if (state.showHint && app == state.visibleApps.firstOrNull()) {
                        LockHint(stringResource(R.string.applock_hint), { onIntent(AppLockListIntent.DismissHint) }, Modifier.align(Alignment.BottomCenter).fillMaxWidth())
                    }
                }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.permissionItem(permissions: AppLockPermissions, onIntent: (AppLockListIntent) -> Unit) {
    when {
        !permissions.usageAccess -> item {
            PermissionCard(
                stringResource(R.string.applock_usage_title),
                stringResource(R.string.applock_usage_message),
                stringResource(R.string.applock_grant),
                { onIntent(AppLockListIntent.GrantUsageAccess) },
                Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            )
        }
        !permissions.overlay -> item {
            PermissionCard(
                stringResource(R.string.applock_overlay_title),
                stringResource(R.string.applock_overlay_message),
                stringResource(R.string.applock_grant),
                { onIntent(AppLockListIntent.GrantOverlay) },
                Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            )
        }
    }
}

@ScreenPreviews
@Composable
private fun AppLockPreview() = VaultPreview {
    AppLockScreen(
        AppLockListState(
            loading = false,
            apps = listOf("Contacts", "Google", "Signal", "Twitter", "Google Assistant", "Calculator", "Calendar").mapIndexed { i, n -> InstalledApp("pkg.$i", n, i == 0) },
            permissions = AppLockPermissions(usageAccess = false, overlay = true, notifications = true),
        ),
        onIntent = {},
    )
}
