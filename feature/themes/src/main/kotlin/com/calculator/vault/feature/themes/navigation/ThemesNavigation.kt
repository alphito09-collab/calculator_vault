package com.calculator.vault.feature.themes.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.calculator.vault.core.navigation.Navigator
import com.calculator.vault.core.navigation.ThemesRoute
import com.calculator.vault.feature.themes.presentation.ui.ThemesRoute as ThemesScreenRoute

fun EntryProviderScope<NavKey>.themesEntries(navigator: Navigator) {
    entry<ThemesRoute> { ThemesScreenRoute(onClose = { navigator.back() }) }
}
