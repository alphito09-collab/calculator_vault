package com.calculator.vault.feature.applock.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.calculator.vault.core.navigation.AppLockRoute
import com.calculator.vault.core.navigation.AppLockSettingsRoute
import com.calculator.vault.core.navigation.Navigator
import com.calculator.vault.core.navigation.PatternPurpose
import com.calculator.vault.core.navigation.PatternSetupRoute
import com.calculator.vault.feature.applock.presentation.ui.AppLockRoute as AppLockScreenRoute
import com.calculator.vault.feature.applock.presentation.ui.AppLockSettingsRoute as AppLockSettingsScreenRoute

fun EntryProviderScope<NavKey>.appLockEntries(navigator: Navigator) {
    entry<AppLockRoute> { AppLockScreenRoute(onOpenSettings = { navigator.navigate(AppLockSettingsRoute) }, onClose = { navigator.back() }) }
    entry<AppLockSettingsRoute> {
        AppLockSettingsScreenRoute(onSetupPattern = { navigator.navigate(PatternSetupRoute(PatternPurpose.APP_LOCK)) }, onClose = { navigator.back() })
    }
}
