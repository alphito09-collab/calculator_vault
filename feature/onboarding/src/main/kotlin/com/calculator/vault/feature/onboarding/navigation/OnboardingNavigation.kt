package com.calculator.vault.feature.onboarding.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.calculator.vault.core.navigation.CreatePinRoute
import com.calculator.vault.core.navigation.Navigator
import com.calculator.vault.core.navigation.OnboardingRoute
import com.calculator.vault.feature.onboarding.presentation.ui.OnboardingRoute as OnboardingScreenRoute

fun EntryProviderScope<NavKey>.onboardingEntries(navigator: Navigator) {
    entry<OnboardingRoute> {
        OnboardingScreenRoute(onFinished = { navigator.resetTo(CreatePinRoute) })
    }
}
