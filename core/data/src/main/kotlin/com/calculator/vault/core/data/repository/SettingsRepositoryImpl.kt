package com.calculator.vault.core.data.repository

import com.calculator.vault.core.datastore.VaultPreferences
import com.calculator.vault.core.datastore.VaultPreferences.BoolKey
import com.calculator.vault.core.domain.repository.SettingsRepository
import com.calculator.vault.core.model.AppIconOption
import com.calculator.vault.core.model.GridSize
import com.calculator.vault.core.model.ThemeMode
import com.calculator.vault.core.model.UserSettings
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single(binds = [SettingsRepository::class])
class SettingsRepositoryImpl(private val prefs: VaultPreferences) : SettingsRepository {
    override val settings: Flow<UserSettings> = prefs.settings

    override suspend fun setThemeMode(mode: ThemeMode) = prefs.update { themeMode = mode }
    override suspend fun setLanguage(tag: String?) = prefs.update { setLanguage(tag) }
    override suspend fun setWelcomeAccepted() = prefs.update { set(BoolKey.WELCOME_ACCEPTED, true) }
    override suspend fun setOnboardingCompleted() = prefs.update { set(BoolKey.ONBOARDING_DONE, true) }
    override suspend fun setRecoveryPromptHandled() = prefs.update { set(BoolKey.RECOVERY_PROMPT_HANDLED, true) }
    override suspend fun setAppIcon(icon: AppIconOption) = prefs.update { appIcon = icon }
    override suspend fun setPanicMode(enabled: Boolean) = prefs.update { set(BoolKey.PANIC, enabled) }
    override suspend fun setIntruderDetection(enabled: Boolean) = prefs.update { set(BoolKey.INTRUDER, enabled) }
    override suspend fun setIntruderSelfie(enabled: Boolean) = prefs.update { set(BoolKey.INTRUDER_SELFIE, enabled) }
    override suspend fun setIntruderAttempts(count: Int) = prefs.update { setIntruderAttempts(count) }
    override suspend fun setPreventScreenshots(enabled: Boolean) = prefs.update { set(BoolKey.PREVENT_SCREENSHOTS, enabled) }
    override suspend fun setNotifications(enabled: Boolean) = prefs.update { set(BoolKey.NOTIFICATIONS, enabled) }
    override suspend fun dismissCloudBanner() = prefs.update { set(BoolKey.CLOUD_BANNER_DISMISSED, true) }
    override suspend fun dismissRecoveryBanner() = prefs.update { set(BoolKey.RECOVERY_BANNER_DISMISSED, true) }
    override suspend fun dismissResetCodeBanner() = prefs.update { set(BoolKey.RESET_CODE_BANNER_DISMISSED, true) }
    override suspend fun setGridSize(size: GridSize) = prefs.update { gridSize = size }
    override suspend fun setGroupByDate(enabled: Boolean) = prefs.update { set(BoolKey.GROUP_BY_DATE, enabled) }
    override suspend fun setSlideshowInterval(seconds: Int) = prefs.update { setSlideshowInterval(seconds) }
}
