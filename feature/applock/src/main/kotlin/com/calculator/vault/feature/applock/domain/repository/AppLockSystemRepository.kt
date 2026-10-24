package com.calculator.vault.feature.applock.domain.repository

import com.calculator.vault.core.model.InstalledApp

data class AppLockPermissions(val usageAccess: Boolean, val overlay: Boolean, val notifications: Boolean) {
    val allGranted: Boolean get() = usageAccess && overlay
}

/** Device-side pieces of App Lock: installed apps and the special permissions it needs. */
interface AppLockSystemRepository {
    suspend fun installedApps(locked: Set<String>): List<InstalledApp>
    fun permissions(): AppLockPermissions
}
