package com.calculator.vault.feature.importer.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.calculator.vault.core.navigation.ImporterRoute
import com.calculator.vault.core.navigation.Navigator
import com.calculator.vault.feature.importer.presentation.ui.ImporterRoute as ImporterScreenRoute

fun EntryProviderScope<NavKey>.importerEntries(navigator: Navigator) {
    entry<ImporterRoute> { route -> ImporterScreenRoute(type = route.filter, onClose = { navigator.back() }) }
}
