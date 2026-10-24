package com.calculator.vault.feature.applock.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** Restarts App Lock after a reboot or app update. */
class BootReceiver : BroadcastReceiver(), KoinComponent {
    private val controller: AppLockController by inject()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            controller.refresh()
        }
    }
}
