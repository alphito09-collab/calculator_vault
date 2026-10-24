package com.calculator.vault.core.model

enum class ThemeMode { SYSTEM, LIGHT, DARK }

enum class LockAfter(val millis: Long) {
    IMMEDIATELY(0),
    SECONDS_30(30_000),
    MINUTE_1(60_000),
    MINUTES_5(300_000),
}

enum class AppLockAuthMethod { PIN, FINGERPRINT, PATTERN }

enum class BackupNetwork { WIFI_ONLY, WIFI_AND_CELLULAR }

enum class GridSize(val columns: Int) { LARGE(2), MEDIUM(3), SMALL(4) }

data class UserSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val languageTag: String? = null,
    val languageChosen: Boolean = false,
    /** "Get Started" (and with it the terms) was accepted on the first-install splash. */
    val welcomeAccepted: Boolean = false,
    val onboardingCompleted: Boolean = false,
    val recoveryPromptHandled: Boolean = false,
    val appIcon: AppIconOption = AppIconOption.DEFAULT,
    val panicModeEnabled: Boolean = false,
    val intruderDetectionEnabled: Boolean = true,
    val intruderSelfieEnabled: Boolean = false,
    val intruderAttempts: Int = 3,
    val preventScreenshots: Boolean = false,
    val fingerprintUnlockEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val cloudBannerDismissed: Boolean = false,
    val recoveryBannerDismissed: Boolean = false,
    val resetCodeBannerDismissed: Boolean = false,
    val gridSize: GridSize = GridSize.MEDIUM,
    val groupByDate: Boolean = true,
    val slideshowIntervalSeconds: Int = 2,
)

data class AppLockSettings(
    val enabled: Boolean = false,
    val lockAfter: LockAfter = LockAfter.IMMEDIATELY,
    val authMethod: AppLockAuthMethod = AppLockAuthMethod.PIN,
    val lockedPackages: Set<String> = emptySet(),
    val hasPattern: Boolean = false,
)

data class BackupSettings(
    val autoBackup: Boolean = false,
    val network: BackupNetwork = BackupNetwork.WIFI_ONLY,
    val accountEmail: String? = null,
    val accountName: String? = null,
    val lastBackupAt: Long? = null,
)
