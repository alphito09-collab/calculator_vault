package com.calculator.vault.feature.intruder.presentation.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.calculator.vault.core.common.session.LockGuard
import com.calculator.vault.core.designsystem.component.IconTile
import com.calculator.vault.core.designsystem.component.LoadingContent
import com.calculator.vault.core.designsystem.component.RowTrailing
import com.calculator.vault.core.designsystem.component.SectionLabel
import com.calculator.vault.core.designsystem.component.SettingsRow
import com.calculator.vault.core.designsystem.component.VaultCardColumn
import com.calculator.vault.core.designsystem.component.VaultDialog
import com.calculator.vault.core.designsystem.component.VaultTextButton
import com.calculator.vault.core.designsystem.component.VaultTopBar
import com.calculator.vault.core.designsystem.icon.VaultIcons
import com.calculator.vault.core.designsystem.preview.ScreenPreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.model.IntruderAttempt
import com.calculator.vault.core.model.IntruderPhotoRequest
import com.calculator.vault.core.model.IntruderStats
import com.calculator.vault.core.ui.component.VaultScreen
import com.calculator.vault.core.ui.mvi.CollectEffects
import com.calculator.vault.feature.intruder.R
import com.calculator.vault.feature.intruder.presentation.components.AttemptLimitPicker
import com.calculator.vault.feature.intruder.presentation.components.AttemptRow
import com.calculator.vault.feature.intruder.presentation.components.NoAttemptsCard
import com.calculator.vault.feature.intruder.presentation.components.StatCard
import com.calculator.vault.feature.intruder.presentation.mvi.IntruderEffect
import com.calculator.vault.feature.intruder.presentation.mvi.IntruderIntent
import com.calculator.vault.feature.intruder.presentation.mvi.IntruderState
import com.calculator.vault.feature.intruder.presentation.mvi.IntruderViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun IntruderRoute(onOpenPhoto: (String) -> Unit, onClose: () -> Unit, viewModel: IntruderViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        viewModel.onIntent(IntruderIntent.CameraPermissionResult(it))
    }
    viewModel.effects.CollectEffects { effect ->
        when (effect) {
            IntruderEffect.RequestCameraPermission -> {
                LockGuard.allowExternalLaunch()
                camera.launch(Manifest.permission.CAMERA)
            }
            is IntruderEffect.OpenPhoto -> onOpenPhoto(effect.attemptId)
            IntruderEffect.Close -> onClose()
        }
    }
    IntruderScreen(state, viewModel::onIntent)
}

@Composable
fun IntruderScreen(state: IntruderState, onIntent: (IntruderIntent) -> Unit) {
    VaultScreen(topBar = { VaultTopBar(stringResource(R.string.intruder_title), onNavigationClick = { onIntent(IntruderIntent.Back) }) }) {
        if (state.loading) {
            LoadingContent()
            return@VaultScreen
        }
        val navBar = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        LazyColumn(
            Modifier.align(Alignment.TopCenter).widthIn(max = 640.dp),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 40.dp + navBar),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                VaultCardColumn {
                    Row(Modifier.padding(start = 16.dp, top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconTile(VaultIcons.ShieldFilled)
                        SettingsRow(
                            title = stringResource(R.string.intruder_title),
                            subtitle = stringResource(R.string.intruder_detection_desc),
                            trailing = RowTrailing.Toggle(state.detectionEnabled) { onIntent(IntruderIntent.Detection(it)) },
                        )
                    }
                    SettingsRow(
                        title = stringResource(R.string.intruder_selfie),
                        subtitle = pluralStringResource(R.plurals.intruder_selfie_desc, state.attemptLimit, state.attemptLimit),
                        icon = VaultIcons.ScanFace,
                        enabled = state.detectionEnabled,
                        trailing = RowTrailing.Toggle(state.selfieEnabled) { onIntent(IntruderIntent.Selfie(it)) },
                        showDivider = true,
                    )
                    AttemptLimitPicker(
                        limit = state.attemptLimit,
                        enabled = state.detectionEnabled,
                        onChange = { onIntent(IntruderIntent.AttemptLimit(it)) },
                    )
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(state.stats.failedThisWeek, stringResource(R.string.intruder_failed_week), Modifier.weight(1f))
                    StatCard(state.stats.photosCaptured, stringResource(R.string.intruder_photos_captured), Modifier.weight(1f))
                }
            }
            item {
                SectionLabel(stringResource(R.string.intruder_attempts), Modifier.padding(top = 8.dp)) {
                    if (state.attempts.isNotEmpty()) VaultTextButton(stringResource(R.string.intruder_clear), { onIntent(IntruderIntent.Clear) }, small = true)
                }
            }
            if (state.attempts.isEmpty()) {
                item { NoAttemptsCard() }
            } else {
                items(state.attempts, key = { it.id }) { attempt ->
                    AttemptRow(attempt, { onIntent(IntruderIntent.OpenPhoto(attempt)) }, Modifier.animateItem())
                }
            }
        }
    }
    if (state.confirmClear) {
        VaultDialog(
            title = stringResource(R.string.intruder_clear_title),
            message = stringResource(R.string.intruder_clear_message),
            confirmText = stringResource(R.string.intruder_clear),
            onConfirm = { onIntent(IntruderIntent.ConfirmClear) },
            onDismissRequest = { onIntent(IntruderIntent.DismissClear) },
            destructive = true,
        )
    }
}

@Composable
fun IntruderPhotoRoute(attemptId: String, onClose: () -> Unit) {
    VaultScreen(topBar = { VaultTopBar(stringResource(R.string.intruder_photo_title), onNavigationClick = onClose) }) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            AsyncImage(IntruderPhotoRequest(attemptId), null, Modifier.fillMaxSize().padding(20.dp), contentScale = ContentScale.Fit)
        }
    }
}

@ScreenPreviews
@Composable
private fun IntruderEmptyPreview() = VaultPreview { IntruderScreen(IntruderState(loading = false), onIntent = {}) }

@ScreenPreviews
@Composable
private fun IntruderListPreview() = VaultPreview {
    IntruderScreen(
        IntruderState(
            loading = false,
            stats = IntruderStats(4, 1),
            attempts = listOf(IntruderAttempt("1", System.currentTimeMillis(), 3, true), IntruderAttempt("2", System.currentTimeMillis() - 86_400_000, 1, false)),
        ),
        onIntent = {},
    )
}

