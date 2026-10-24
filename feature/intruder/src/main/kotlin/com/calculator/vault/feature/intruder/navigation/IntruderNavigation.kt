package com.calculator.vault.feature.intruder.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.calculator.vault.core.navigation.IntruderPhotoRoute
import com.calculator.vault.core.navigation.IntruderRoute
import com.calculator.vault.core.navigation.Navigator
import com.calculator.vault.feature.intruder.presentation.ui.IntruderPhotoRoute as IntruderPhotoScreen
import com.calculator.vault.feature.intruder.presentation.ui.IntruderRoute as IntruderScreenRoute

fun EntryProviderScope<NavKey>.intruderEntries(navigator: Navigator) {
    entry<IntruderRoute> { IntruderScreenRoute(onOpenPhoto = { navigator.navigate(IntruderPhotoRoute(it)) }, onClose = { navigator.back() }) }
    entry<IntruderPhotoRoute> { route -> IntruderPhotoScreen(route.attemptId, onClose = { navigator.back() }) }
}
