package com.calculator.vault.feature.settings.data

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import com.calculator.vault.feature.settings.R

/**
 * Device admin used only for uninstall protection: while active, Android refuses to uninstall
 * the app until the admin is turned off in system settings.
 */
class VaultDeviceAdminReceiver : DeviceAdminReceiver() {
    override fun onDisableRequested(context: Context, intent: Intent): CharSequence =
        context.getString(R.string.settings_admin_disable_warning)
}
