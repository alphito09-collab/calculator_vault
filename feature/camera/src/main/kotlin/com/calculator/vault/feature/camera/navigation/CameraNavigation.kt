package com.calculator.vault.feature.camera.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.calculator.vault.core.navigation.CameraRoute
import com.calculator.vault.core.navigation.Navigator
import com.calculator.vault.feature.camera.presentation.ui.CameraRoute as CameraScreenRoute

fun EntryProviderScope<NavKey>.cameraEntries(navigator: Navigator) {
    entry<CameraRoute> { route -> CameraScreenRoute(route.startWithVideo, onClose = { navigator.back() }) }
}
