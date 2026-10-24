package com.calculator.vault.feature.settings.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.calculator.vault.core.navigation.AppLockRoute
import com.calculator.vault.core.navigation.AppLockSettingsRoute
import com.calculator.vault.core.navigation.ChangeAppIconRoute
import com.calculator.vault.core.navigation.ChangePinRoute
import com.calculator.vault.core.navigation.CloudBackupRoute
import com.calculator.vault.core.navigation.IntruderRoute
import com.calculator.vault.core.navigation.LanguageRoute
import com.calculator.vault.core.navigation.Navigator
import com.calculator.vault.core.navigation.RecoveryMethodRoute
import com.calculator.vault.core.navigation.ThemesRoute
import com.calculator.vault.core.navigation.TrashRoute
import com.calculator.vault.feature.settings.presentation.mvi.SettingsDestination
import com.calculator.vault.feature.settings.presentation.ui.AppIconRoute
import com.calculator.vault.feature.settings.presentation.ui.SettingsTab

/** The Settings tab composable, handed to the vault shell by the app module. */
@Composable
fun SettingsTabContent(navigator: Navigator, contentPadding: PaddingValues, versionLabel: String) {
    SettingsTab(contentPadding, versionLabel, onNavigate = { destination ->
        navigator.navigate(
            when (destination) {
                SettingsDestination.ACCOUNT, SettingsDestination.CLOUD_BACKUP -> CloudBackupRoute
                SettingsDestination.THEME -> ThemesRoute
                SettingsDestination.APP_ICON -> ChangeAppIconRoute
                SettingsDestination.LANGUAGE -> LanguageRoute(fromSettings = true)
                SettingsDestination.APP_LOCK -> AppLockRoute
                SettingsDestination.CHANGE_PIN -> ChangePinRoute
                SettingsDestination.APP_LOCK_SECURITY -> AppLockSettingsRoute
                SettingsDestination.RECOVERY -> RecoveryMethodRoute(fromSettings = true)
                SettingsDestination.INTRUDER -> IntruderRoute
                SettingsDestination.TRASH -> TrashRoute
            },
        )
    })
}

fun EntryProviderScope<NavKey>.settingsEntries(navigator: Navigator) {
    entry<ChangeAppIconRoute> { AppIconRoute(onClose = { navigator.back() }) }
}
