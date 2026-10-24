package com.calculator.vault.feature.splash.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.calculator.vault.core.navigation.CalculatorRoute
import com.calculator.vault.core.navigation.CreatePinRoute
import com.calculator.vault.core.navigation.LanguageRoute
import com.calculator.vault.core.navigation.Navigator
import com.calculator.vault.core.navigation.OnboardingRoute
import com.calculator.vault.core.navigation.SplashRoute
import com.calculator.vault.feature.splash.domain.usecase.StartDestination
import com.calculator.vault.feature.splash.presentation.ui.SplashRoute as SplashScreenRoute

fun EntryProviderScope<NavKey>.splashEntries(navigator: Navigator) {
    entry<SplashRoute> {
        SplashScreenRoute(
            onNavigate = { destination ->
                navigator.resetTo(
                    when (destination) {
                        StartDestination.WELCOME, StartDestination.LANGUAGE -> LanguageRoute()
                        StartDestination.ONBOARDING -> OnboardingRoute
                        StartDestination.CREATE_PIN -> CreatePinRoute
                        StartDestination.CALCULATOR -> CalculatorRoute
                    },
                )
            },
            // Pushed (not reset) so back from the language list returns to the welcome.
            onGetStarted = { navigator.navigate(LanguageRoute()) },
        )
    }
}
