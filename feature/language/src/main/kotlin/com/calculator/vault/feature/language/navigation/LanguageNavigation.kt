package com.calculator.vault.feature.language.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.calculator.vault.core.navigation.LanguageRoute
import com.calculator.vault.core.navigation.Navigator
import com.calculator.vault.core.navigation.OnboardingRoute
import com.calculator.vault.feature.language.presentation.ui.LanguageRoute as LanguageScreenRoute

fun EntryProviderScope<NavKey>.languageEntries(navigator: Navigator) {
    entry<LanguageRoute> { route ->
        LanguageScreenRoute(
            fromSettings = route.fromSettings,
            onContinue = { navigator.resetTo(OnboardingRoute) },
            onClose = { navigator.back() },
        )
    }
}
