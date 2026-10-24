package com.calculator.vault.feature.applock.domain.usecase

import com.calculator.vault.core.domain.repository.AppLockRepository
import com.calculator.vault.core.domain.repository.AuthRepository
import com.calculator.vault.core.model.AppLockAuthMethod
import com.calculator.vault.core.model.AppLockSettings
import com.calculator.vault.core.model.InstalledApp
import com.calculator.vault.core.model.LockAfter
import com.calculator.vault.feature.applock.domain.repository.AppLockPermissions
import com.calculator.vault.feature.applock.domain.repository.AppLockSystemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory

@Factory
class ObserveAppLockSettingsUseCase(private val repository: AppLockRepository) {
    operator fun invoke(): Flow<AppLockSettings> = repository.settings
}

@Factory
class LoadInstalledAppsUseCase(private val repository: AppLockRepository, private val system: AppLockSystemRepository) {
    suspend operator fun invoke(): List<InstalledApp> = system.installedApps(repository.settings.first().lockedPackages)
}

@Factory
class AppLockPermissionsUseCase(private val system: AppLockSystemRepository) {
    operator fun invoke(): AppLockPermissions = system.permissions()
}

/** Locking the first app also switches App Lock on. */
@Factory
class ToggleAppLockedUseCase(private val repository: AppLockRepository) {
    suspend operator fun invoke(packageName: String, locked: Boolean) {
        repository.setLocked(packageName, locked)
        if (locked && !repository.settings.first().enabled) repository.setEnabled(true)
    }
}

@Factory
class UpdateAppLockSettingsUseCase(private val repository: AppLockRepository) {
    suspend fun setEnabled(enabled: Boolean) = repository.setEnabled(enabled)
    suspend fun setLockAfter(lockAfter: LockAfter) = repository.setLockAfter(lockAfter)
    suspend fun setMethod(method: AppLockAuthMethod) = repository.setAuthMethod(method)
}

/** Verifies the credential chosen for App Lock (PIN = vault PIN). */
@Factory
class VerifyAppLockCredentialUseCase(private val auth: AuthRepository, private val repository: AppLockRepository) {
    suspend fun pin(pin: String): Boolean = auth.verifyPin(pin)
    suspend fun pattern(pattern: List<Int>): Boolean = repository.verifyAppLockPattern(pattern)
    fun markUnlocked(packageName: String) = repository.markUnlocked(packageName)
}
