package com.calculator.vault.feature.calculator.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.calculator.vault.core.navigation.CalculatorRoute
import com.calculator.vault.core.navigation.ForgotPinRoute
import com.calculator.vault.core.navigation.Navigator
import com.calculator.vault.core.navigation.ResetPinRoute
import com.calculator.vault.feature.calculator.presentation.ui.CalculatorRoute as CalculatorScreenRoute

fun EntryProviderScope<NavKey>.calculatorEntries(navigator: Navigator) {
    entry<CalculatorRoute> {
        CalculatorScreenRoute(
            onOpenVault = { navigator.openVault() },
            onForgotPin = { method -> navigator.navigate(ForgotPinRoute(method)) },
            onResetPin = { navigator.resetTo(ResetPinRoute()) },
        )
    }
}
