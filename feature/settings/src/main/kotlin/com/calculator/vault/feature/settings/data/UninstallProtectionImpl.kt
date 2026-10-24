package com.calculator.vault.feature.settings.data

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.core.content.getSystemService
import com.calculator.vault.feature.settings.domain.repository.UninstallProtection
import org.koin.core.annotation.Single

@Single(binds = [UninstallProtection::class])
class UninstallProtectionImpl(private val context: Context) : UninstallProtection {
    private val admin get() = ComponentName(context, VaultDeviceAdminReceiver::class.java)
    private val dpm get() = context.getSystemService<DevicePolicyManager>()

    override fun isActive(): Boolean = dpm?.isAdminActive(admin) == true

    override fun activationIntent(explanation: String): Intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        .putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin)
        .putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, explanation)

    override fun deactivate() {
        runCatching { dpm?.removeActiveAdmin(admin) }
    }
}
