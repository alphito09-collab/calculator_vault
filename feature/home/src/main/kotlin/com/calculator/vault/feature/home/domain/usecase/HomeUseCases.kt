package com.calculator.vault.feature.home.domain.usecase

import com.calculator.vault.core.domain.repository.CloudBackupRepository
import com.calculator.vault.core.domain.repository.RecoveryRepository
import com.calculator.vault.core.domain.repository.SettingsRepository
import com.calculator.vault.core.domain.repository.StorageRepository
import com.calculator.vault.core.domain.repository.VaultRepository
import com.calculator.vault.core.model.VaultCounts
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.koin.core.annotation.Factory

enum class HomeBanner { ADD_RECOVERY, ENABLE_BACKUP, RESET_CODE }

data class HomeSummary(val counts: VaultCounts, val banner: HomeBanner?)

/**
 * Item counts plus which banner (if any) to show. They take turns in one slot: add a recovery
 * method, then cloud backup, then the PIN reset code, each until dismissed.
 */
@Factory
class ObserveHomeSummaryUseCase(
    private val vault: VaultRepository,
    private val settings: SettingsRepository,
    private val recovery: RecoveryRepository,
    private val backup: CloudBackupRepository,
) {
    operator fun invoke(): Flow<HomeSummary> = combine(
        vault.observeCounts(),
        settings.settings,
        recovery.options,
        backup.settings,
    ) { counts, s, options, backupSettings ->
        val banner = when {
            !options.hasAnyMethod && !s.recoveryBannerDismissed -> HomeBanner.ADD_RECOVERY
            backupSettings.accountEmail == null && !s.cloudBannerDismissed -> HomeBanner.ENABLE_BACKUP
            !s.resetCodeBannerDismissed -> HomeBanner.RESET_CODE
            else -> null
        }
        HomeSummary(counts, banner)
    }
}

@Factory
class DismissBannerUseCase(private val settings: SettingsRepository) {
    suspend operator fun invoke(banner: HomeBanner) = when (banner) {
        HomeBanner.ADD_RECOVERY -> settings.dismissRecoveryBanner()
        HomeBanner.ENABLE_BACKUP -> settings.dismissCloudBanner()
        HomeBanner.RESET_CODE -> settings.dismissResetCodeBanner()
    }
}

@Factory
class VaultStorageUseCase(private val storage: StorageRepository) {
    fun hasAccess(): Boolean = storage.hasAllFilesAccess()
    suspend fun previousVault(): StorageRepository.PreviousVault? = storage.findPreviousVault()
    suspend fun adopt(pin: String) = storage.adoptPreviousVault(pin)
    suspend fun archive() = storage.archivePreviousVault()
    suspend fun syncConfig() = storage.syncConfigToSharedStorage()
}
