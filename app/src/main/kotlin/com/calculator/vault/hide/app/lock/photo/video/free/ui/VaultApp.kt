package com.calculator.vault.hide.app.lock.photo.video.free.ui

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.calculator.vault.core.designsystem.component.VaultSnackbarHost
import com.calculator.vault.core.designsystem.component.showToast
import com.calculator.vault.core.designsystem.theme.CalculatorVaultTheme
import com.calculator.vault.core.designsystem.theme.VaultTheme
import com.calculator.vault.core.model.AppIconOption
import com.calculator.vault.core.model.ThemeMode
import com.calculator.vault.core.navigation.Navigator
import com.calculator.vault.core.ui.component.PrivacyCover
import com.calculator.vault.core.ui.util.AppToasts
import com.calculator.vault.core.ui.util.SystemBarsAppearance
import com.calculator.vault.feature.applock.navigation.appLockEntries
import com.calculator.vault.feature.audios.navigation.audiosEntries
import com.calculator.vault.feature.calculator.navigation.calculatorEntries
import com.calculator.vault.feature.camera.navigation.cameraEntries
import com.calculator.vault.feature.cloudbackup.navigation.cloudBackupEntries
import com.calculator.vault.feature.files.navigation.filesEntries
import com.calculator.vault.feature.home.navigation.homeEntries
import com.calculator.vault.feature.importer.navigation.importerEntries
import com.calculator.vault.feature.intruder.navigation.intruderEntries
import com.calculator.vault.feature.language.navigation.languageEntries
import com.calculator.vault.feature.onboarding.navigation.onboardingEntries
import com.calculator.vault.feature.photos.navigation.photosEntries
import com.calculator.vault.feature.pin.navigation.pinEntries
import com.calculator.vault.feature.recovery.navigation.recoveryEntries
import com.calculator.vault.feature.settings.navigation.SettingsTabContent
import com.calculator.vault.feature.settings.navigation.settingsEntries
import com.calculator.vault.feature.splash.navigation.splashEntries
import com.calculator.vault.feature.themes.navigation.themesEntries
import com.calculator.vault.feature.trash.navigation.trashEntries
import com.calculator.vault.feature.videos.navigation.videosEntries

/**
 * Root composable: theme, the Navigation 3 host with shared transitions, the app-wide
 * toast host and the privacy cover drawn while the vault is visible to system UI.
 */
@Composable
fun VaultApp(
    navigator: Navigator,
    themeMode: ThemeMode,
    appIcon: AppIconOption,
    privacyCover: Boolean,
    versionLabel: String,
    pinChangedMessage: String,
    onExit: () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    CalculatorVaultTheme(darkTheme = dark) {
        SystemBarsAppearance(dark)
        val snackbar = remember { SnackbarHostState() }
        LaunchedEffect(Unit) { AppToasts.events.collect { snackbar.showToast(it.message, it.kind) } }
        val direction = if (LocalLayoutDirection.current == LayoutDirection.Rtl) -1 else 1
        Box(Modifier.fillMaxSize().background(VaultTheme.scheme.background)) {
            NavDisplay(
                backStack = navigator.backStack,
                onBack = { if (!navigator.back()) onExit() },
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
                transitionSpec = { forward(direction) },
                popTransitionSpec = { backward(direction) },
                predictivePopTransitionSpec = { backward(direction) },
                entryProvider = entryProvider {
                    splashEntries(navigator)
                    languageEntries(navigator)
                    onboardingEntries(navigator)
                    pinEntries(navigator, onPinChanged = { AppToasts.show(pinChangedMessage) })
                    calculatorEntries(navigator)
                    recoveryEntries(navigator)
                    homeEntries(navigator) { padding -> SettingsTabContent(navigator, padding, versionLabel) }
                    importerEntries(navigator)
                    photosEntries(navigator)
                    videosEntries(navigator)
                    audiosEntries(navigator)
                    filesEntries(navigator)
                    cameraEntries(navigator)
                    trashEntries(navigator)
                    settingsEntries(navigator)
                    themesEntries(navigator)
                    appLockEntries(navigator)
                    intruderEntries(navigator)
                    cloudBackupEntries(navigator)
                },
            )
            VaultSnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 96.dp))
            // Drawn without animation: the very next frame must not contain vault content.
            if (privacyCover) PrivacyCover(appIcon)
        }
    }
}

private const val DURATION = 340

/** New screen slides in from the end while the old one drifts back and fades. */
private fun forward(direction: Int): ContentTransform =
    (slideInHorizontally(tween(DURATION, easing = FastOutSlowInEasing)) { direction * it / 3 } + fadeIn(tween(DURATION))) togetherWith
        (slideOutHorizontally(tween(DURATION, easing = FastOutSlowInEasing)) { -direction * it / 6 } + fadeOut(tween(DURATION / 2)))

private fun backward(direction: Int): ContentTransform =
    (slideInHorizontally(tween(DURATION, easing = FastOutSlowInEasing)) { -direction * it / 6 } + fadeIn(tween(DURATION))) togetherWith
        (slideOutHorizontally(tween(DURATION, easing = FastOutSlowInEasing)) { direction * it / 3 } + fadeOut(tween(DURATION / 2)))
