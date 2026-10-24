package com.calculator.vault.feature.cloudbackup.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.calculator.vault.core.navigation.CloudBackupRoute
import com.calculator.vault.core.navigation.Navigator
import com.calculator.vault.feature.cloudbackup.presentation.ui.CloudBackupRoute as CloudBackupScreenRoute

fun EntryProviderScope<NavKey>.cloudBackupEntries(navigator: Navigator) {
    entry<CloudBackupRoute> { CloudBackupScreenRoute(onClose = { navigator.back() }) }
}
