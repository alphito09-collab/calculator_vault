package com.calculator.vault.feature.home.presentation.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calculator.vault.core.designsystem.component.BottomBarFabCenterFromBottom
import com.calculator.vault.core.designsystem.component.VaultBottomBar
import com.calculator.vault.core.designsystem.component.VaultSnackbarHost
import com.calculator.vault.core.designsystem.component.showToast
import com.calculator.vault.core.designsystem.icon.VaultIcons
import com.calculator.vault.core.designsystem.preview.ScreenPreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultTheme
import com.calculator.vault.core.model.MediaType
import com.calculator.vault.core.model.VaultCounts
import com.calculator.vault.core.navigation.MainTab
import com.calculator.vault.core.ui.component.ExitAppDialog
import com.calculator.vault.core.ui.component.FilesPermissionDialog
import com.calculator.vault.core.ui.mvi.CollectEffects
import com.calculator.vault.core.ui.util.ExternalIntents
import com.calculator.vault.feature.home.R
import com.calculator.vault.feature.home.presentation.components.AddFab
import com.calculator.vault.feature.home.presentation.components.AddOption
import com.calculator.vault.feature.home.presentation.components.RadialAddMenu
import com.calculator.vault.feature.home.presentation.components.RestoreVaultDialog
import com.calculator.vault.feature.home.presentation.mvi.AddAction
import com.calculator.vault.feature.home.presentation.mvi.HomeEffect
import com.calculator.vault.feature.home.presentation.mvi.HomeIntent
import com.calculator.vault.feature.home.presentation.mvi.HomeState
import com.calculator.vault.feature.home.presentation.mvi.HomeTool
import com.calculator.vault.feature.home.presentation.mvi.HomeViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/** Navigation callbacks out of the vault home. */
class MainCallbacks(
    val onOpenCategory: (MediaType) -> Unit,
    val onImport: (MediaType) -> Unit,
    val onOpenCamera: () -> Unit,
    val onOpenTool: (HomeTool) -> Unit,
    val onOpenRecovery: () -> Unit,
    val onOpenCloudBackup: () -> Unit,
)

/**
 * The vault shell: Home and Settings tabs, the bottom bar and the radial add menu.
 * [settingsContent] is supplied by the settings feature through the app module.
 */
@Composable
fun MainRoute(
    initialTab: MainTab,
    callbacks: MainCallbacks,
    settingsContent: @Composable (PaddingValues) -> Unit,
    viewModel: HomeViewModel = koinViewModel(key = "main") { parametersOf(initialTab) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = LocalActivity.current
    val snackbar = remember { SnackbarHostState() }
    val restoredText = stringResource(R.string.home_restored)
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.onIntent(HomeIntent.Resumed) }
    viewModel.effects.CollectEffects { effect ->
        when (effect) {
            is HomeEffect.OpenCategory -> callbacks.onOpenCategory(effect.type)
            is HomeEffect.Import -> callbacks.onImport(effect.type)
            HomeEffect.OpenCamera -> callbacks.onOpenCamera()
            is HomeEffect.OpenTool -> callbacks.onOpenTool(effect.tool)
            HomeEffect.OpenRecovery -> callbacks.onOpenRecovery()
            HomeEffect.OpenCloudBackup -> callbacks.onOpenCloudBackup()
            HomeEffect.OpenFilesAccessSettings -> ExternalIntents.openAllFilesAccessSettings(context)
            HomeEffect.VaultRestored -> snackbar.showToast(restoredText)
            HomeEffect.ExitApp -> activity?.finish()
        }
    }
    BackHandler { viewModel.onIntent(HomeIntent.BackPressed) }
    MainScreen(state, viewModel::onIntent, settingsContent, snackbar)
}

@Composable
fun MainScreen(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
    settingsContent: @Composable (PaddingValues) -> Unit,
    snackbar: SnackbarHostState = remember { SnackbarHostState() },
) {
    val contentPadding = PaddingValues(bottom = 150.dp)
    Box(Modifier.fillMaxSize().background(VaultTheme.scheme.background)) {
        AnimatedContent(
            targetState = state.tab,
            transitionSpec = {
                val forward = targetState.ordinal > initialState.ordinal
                (slideInHorizontally(tween(260)) { if (forward) it / 6 else -it / 6 } + fadeIn(tween(260))) togetherWith
                    (slideOutHorizontally(tween(200)) { if (forward) -it / 6 else it / 6 } + fadeOut(tween(200)))
            },
            label = "mainTab",
            modifier = Modifier.fillMaxSize(),
        ) { tab ->
            when (tab) {
                MainTab.HOME -> HomeContent(state, onIntent, contentPadding)
                MainTab.SETTINGS -> settingsContent(contentPadding)
            }
        }
        // Tab content scrolls under the floating bar and the transparent navigation bar: fade it
        // out so rows never show through behind the bar or the system buttons.
        val bg = VaultTheme.scheme.background
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Brush.verticalGradient(0f to Color.Transparent, 0.4f to bg.copy(alpha = 0.95f), 0.55f to bg))
                .navigationBarsPadding()
                .height(128.dp),
        )
        VaultBottomBar(
            homeSelected = state.tab == MainTab.HOME,
            onHomeClick = { onIntent(HomeIntent.SelectTab(MainTab.HOME)) },
            onSettingsClick = { onIntent(HomeIntent.SelectTab(MainTab.SETTINGS)) },
            onAddClick = { onIntent(HomeIntent.ToggleAddMenu) },
            modifier = Modifier.align(Alignment.BottomCenter),
            // The real "+" is drawn above the menu scrim below; keep its slot empty here.
            addContent = { Spacer(Modifier.size(64.dp)) },
        )
        RadialAddMenu(
            open = state.addMenuOpen,
            options = addOptions(),
            onSelect = { onIntent(HomeIntent.Add(it)) },
            onDismiss = { onIntent(HomeIntent.CloseAddMenu) },
        )
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = BottomBarFabCenterFromBottom - 32.dp),
        ) {
            AddFab(state.addMenuOpen, { onIntent(HomeIntent.ToggleAddMenu) }, stringResource(R.string.home_add))
        }
        // Above the floating bar and its "+" button.
        VaultSnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 136.dp))
    }
    if (state.showPermissionDialog) {
        FilesPermissionDialog(onAllow = { onIntent(HomeIntent.AllowFilesAccess) }, onDeny = { onIntent(HomeIntent.DenyFilesAccess) })
    }
    state.previousVault?.let { previous ->
        RestoreVaultDialog(
            previous = previous,
            pin = state.restorePin,
            error = state.restoreError,
            restoring = state.restoring,
            onPinChange = { onIntent(HomeIntent.RestorePinChanged(it)) },
            onRestore = { onIntent(HomeIntent.RestorePreviousVault) },
            onStartFresh = { onIntent(HomeIntent.StartFresh) },
        )
    }
    if (state.showExitDialog) {
        ExitAppDialog(onExit = { onIntent(HomeIntent.ConfirmExit) }, onDismiss = { onIntent(HomeIntent.DismissExit) })
    }
}

@Composable
private fun addOptions(): List<AddOption> = listOf(
    AddOption(AddAction.CAMERA, VaultIcons.Camera, stringResource(R.string.home_add_camera), VaultTheme.scheme.primary),
    AddOption(AddAction.MUSIC, VaultIcons.Music, stringResource(R.string.home_add_music), VaultTheme.colors.audioAccent),
    AddOption(AddAction.FILES, VaultIcons.File, stringResource(R.string.home_add_files), VaultTheme.colors.fileAccent),
    AddOption(AddAction.VIDEOS, VaultIcons.Video, stringResource(R.string.home_add_videos), VaultTheme.colors.videoAccent),
    AddOption(AddAction.PHOTOS, VaultIcons.Photo, stringResource(R.string.home_add_photo), VaultTheme.colors.photoAccent),
)

@ScreenPreviews
@Composable
private fun MainScreenPreview() = VaultPreview {
    MainScreen(HomeState(counts = VaultCounts(128, 24, 9, 17)), onIntent = {}, settingsContent = {})
}

@ScreenPreviews
@Composable
private fun MainScreenMenuPreview() = VaultPreview {
    MainScreen(HomeState(counts = VaultCounts(128, 24, 9, 17), addMenuOpen = true), onIntent = {}, settingsContent = {})
}
