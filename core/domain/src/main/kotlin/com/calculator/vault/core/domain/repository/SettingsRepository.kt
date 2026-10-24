package com.calculator.vault.core.domain.repository

import com.calculator.vault.core.model.AppIconOption
import com.calculator.vault.core.model.GridSize
import com.calculator.vault.core.model.ThemeMode
import com.calculator.vault.core.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<UserSettings>

    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setLanguage(tag: String?)
    suspend fun setWelcomeAccepted()
    suspend fun setOnboardingCompleted()
    suspend fun setRecoveryPromptHandled()
    suspend fun setAppIcon(icon: AppIconOption)
    suspend fun setPanicMode(enabled: Boolean)
    suspend fun setIntruderDetection(enabled: Boolean)
    suspend fun setIntruderSelfie(enabled: Boolean)
    suspend fun setIntruderAttempts(count: Int)
    suspend fun setPreventScreenshots(enabled: Boolean)
    suspend fun setNotifications(enabled: Boolean)
    suspend fun dismissCloudBanner()
    suspend fun dismissRecoveryBanner()
    suspend fun dismissResetCodeBanner()
    suspend fun setGridSize(size: GridSize)
    suspend fun setGroupByDate(enabled: Boolean)
    suspend fun setSlideshowInterval(seconds: Int)
}
