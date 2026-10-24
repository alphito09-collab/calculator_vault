package com.calculator.vault.feature.settings.domain.usecase

import com.calculator.vault.core.domain.repository.AppLockRepository
import com.calculator.vault.core.domain.repository.AuthRepository
import com.calculator.vault.core.domain.repository.CloudBackupRepository
import com.calculator.vault.core.domain.repository.IntruderCamera
import com.calculator.vault.core.domain.repository.RecoveryRepository
import com.calculator.vault.core.domain.repository.SettingsRepository
import com.calculator.vault.core.domain.repository.VaultRepository
import com.calculator.vault.core.model.AppIconOption
import com.calculator.vault.core.model.AppLockSettings
import com.calculator.vault.core.model.BackupSettings
import com.calculator.vault.core.model.RecoveryOptions
import com.calculator.vault.core.model.UserSettings
import com.calculator.vault.feature.settings.domain.repository.AppIconSwitcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import javax.crypto.Cipher

data class SettingsOverview(
    val settings: UserSettings,
    val appLock: AppLockSettings,
    val backup: BackupSettings,
    val recovery: RecoveryOptions,
    val fingerprintUnlock: Boolean,
    val trashCount: Int,
)

@Factory
class ObserveSettingsOverviewUseCase(
    private val settings: SettingsRepository,
    private val appLock: AppLockRepository,
    private val backup: CloudBackupRepository,
    private val recovery: RecoveryRepository,
    private val auth: AuthRepository,
    private val vault: VaultRepository,
) {
    operator fun invoke(): Flow<SettingsOverview> = combine(
        settings.settings,
        appLock.settings,
        backup.settings,
        recovery.options,
        combine(auth.isBiometricUnlockEnabled, vault.observeTrash().map { it.size }) { a, b -> a to b },
    ) { s, lock, b, r, (fingerprint, trash) -> SettingsOverview(s, lock, b, r, fingerprint, trash) }
}

@Factory
class UpdateSettingsUseCase(private val settings: SettingsRepository, private val camera: IntruderCamera) {
    suspend fun panic(enabled: Boolean) = settings.setPanicMode(enabled)
    suspend fun intruderSelfie(enabled: Boolean) = settings.setIntruderSelfie(enabled)
    suspend fun preventScreenshots(enabled: Boolean) = settings.setPreventScreenshots(enabled)
    suspend fun notifications(enabled: Boolean) = settings.setNotifications(enabled)
    suspend fun dismissCloudBanner() = settings.dismissCloudBanner()
    fun hasCameraPermission() = camera.hasPermission()
}

@Factory
class FingerprintUnlockUseCase(private val auth: AuthRepository) {
    fun enrollCipher(): Cipher? = auth.biometricEnrollCipher()
    suspend fun enable(cipher: Cipher) = auth.enableBiometricUnlock(cipher)
    suspend fun disable() = auth.disableBiometricUnlock()
}

@Factory
class ChangeAppIconUseCase(private val switcher: AppIconSwitcher, private val settings: SettingsRepository) {
    fun current(): AppIconOption = switcher.current()

    suspend operator fun invoke(option: AppIconOption) {
        settings.setAppIcon(option)
        switcher.apply(option)
    }
}
