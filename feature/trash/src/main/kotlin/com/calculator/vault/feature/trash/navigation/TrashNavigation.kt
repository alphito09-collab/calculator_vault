package com.calculator.vault.feature.trash.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.calculator.vault.core.navigation.Navigator
import com.calculator.vault.core.navigation.TrashRoute
import com.calculator.vault.feature.trash.presentation.ui.TrashRoute as TrashScreenRoute

fun EntryProviderScope<NavKey>.trashEntries(navigator: Navigator) {
    entry<TrashRoute> { TrashScreenRoute(onClose = { navigator.back() }) }
}
