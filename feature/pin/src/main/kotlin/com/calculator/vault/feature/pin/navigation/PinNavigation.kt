package com.calculator.vault.feature.pin.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.calculator.vault.core.navigation.ChangePinRoute
import com.calculator.vault.core.navigation.CreatePinRoute
import com.calculator.vault.core.navigation.Navigator
import com.calculator.vault.core.navigation.RecoveryMethodRoute
import com.calculator.vault.core.navigation.ResetPinRoute
import com.calculator.vault.feature.pin.presentation.mvi.PinPadMode
import com.calculator.vault.feature.pin.presentation.ui.CreatePinRoute as CreatePinScreenRoute
import com.calculator.vault.feature.pin.presentation.ui.PinPadRoute

/**
 * @param onPinChanged shown after settings → change PIN succeeds (e.g. a toast).
 */
fun EntryProviderScope<NavKey>.pinEntries(navigator: Navigator, onPinChanged: () -> Unit) {
    entry<CreatePinRoute> {
        CreatePinScreenRoute(onCreated = { navigator.resetTo(RecoveryMethodRoute()) })
    }
    entry<ChangePinRoute> {
        PinPadRoute(
            mode = PinPadMode.CHANGE,
            onDone = {
                navigator.back()
                onPinChanged()
            },
            onClose = { navigator.back() },
        )
    }
    entry<ResetPinRoute> {
        PinPadRoute(
            mode = PinPadMode.RESET,
            onDone = { navigator.openVault() },
            onClose = { navigator.openVault() },
        )
    }
}
