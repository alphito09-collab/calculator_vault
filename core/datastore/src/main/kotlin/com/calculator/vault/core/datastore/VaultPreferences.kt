package com.calculator.vault.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.calculator.vault.core.common.VaultConstants
import com.calculator.vault.core.model.AppIconOption
import com.calculator.vault.core.model.AppLockAuthMethod
import com.calculator.vault.core.model.AppLockSettings
import com.calculator.vault.core.model.BackupNetwork
import com.calculator.vault.core.model.BackupSettings
import com.calculator.vault.core.model.GridSize
import com.calculator.vault.core.model.LockAfter
import com.calculator.vault.core.model.ThemeMode
import com.calculator.vault.core.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single
import timber.log.Timber
import java.io.IOException

/**
 * User-facing settings. Lives in `files/datastore/vault_settings.preferences_pb`, which is
 * included in Android Auto Backup so preferences come back after a reinstall.
 */
@Single
class VaultPreferences(context: Context) {

    private val store: DataStore<Preferences> = PreferenceDataStoreFactory.create(
        corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
        produceFile = { context.preferencesDataStoreFile(FILE_NAME) },
    )

    private val data: Flow<Preferences> = store.data.catch { error ->
        if (error is IOException) {
            Timber.e(error, "Reading preferences failed")
            emit(emptyPreferences())
        } else throw error
    }

    val settings: Flow<UserSettings> = data.map { p ->
        UserSettings(
            themeMode = p[Keys.THEME].toEnum(ThemeMode.SYSTEM),
            languageTag = p[Keys.LANGUAGE],
            languageChosen = p[Keys.LANGUAGE_CHOSEN] ?: false,
            welcomeAccepted = p[Keys.WELCOME_ACCEPTED] ?: false,
            onboardingCompleted = p[Keys.ONBOARDING_DONE] ?: false,
            recoveryPromptHandled = p[Keys.RECOVERY_PROMPT_HANDLED] ?: false,
            appIcon = p[Keys.APP_ICON].toEnum(AppIconOption.DEFAULT),
            panicModeEnabled = p[Keys.PANIC] ?: false,
            intruderDetectionEnabled = p[Keys.INTRUDER] ?: true,
            intruderSelfieEnabled = p[Keys.INTRUDER_SELFIE] ?: false,
            intruderAttempts = (p[Keys.INTRUDER_ATTEMPTS] ?: VaultConstants.DEFAULT_INTRUDER_ATTEMPTS)
                .coerceIn(VaultConstants.MIN_INTRUDER_ATTEMPTS, VaultConstants.MAX_INTRUDER_ATTEMPTS),
            preventScreenshots = p[Keys.PREVENT_SCREENSHOTS] ?: false,
            fingerprintUnlockEnabled = p[Keys.FINGERPRINT_UNLOCK] ?: false,
            notificationsEnabled = p[Keys.NOTIFICATIONS] ?: true,
            cloudBannerDismissed = p[Keys.CLOUD_BANNER_DISMISSED] ?: false,
            recoveryBannerDismissed = p[Keys.RECOVERY_BANNER_DISMISSED] ?: false,
            resetCodeBannerDismissed = p[Keys.RESET_CODE_BANNER_DISMISSED] ?: false,
            gridSize = p[Keys.GRID_SIZE].toEnum(GridSize.MEDIUM),
            groupByDate = p[Keys.GROUP_BY_DATE] ?: true,
            slideshowIntervalSeconds = p[Keys.SLIDESHOW_INTERVAL] ?: 2,
        )
    }.distinctUntilChanged()

    val appLock: Flow<AppLockSettings> = data.map { p ->
        AppLockSettings(
            enabled = p[Keys.APP_LOCK_ENABLED] ?: false,
            lockAfter = p[Keys.APP_LOCK_AFTER].toEnum(LockAfter.IMMEDIATELY),
            authMethod = p[Keys.APP_LOCK_METHOD].toEnum(AppLockAuthMethod.PIN),
            lockedPackages = p[Keys.APP_LOCK_PACKAGES] ?: emptySet(),
            hasPattern = p[Keys.APP_LOCK_PATTERN] != null,
        )
    }.distinctUntilChanged()

    val backup: Flow<BackupSettings> = data.map { p ->
        BackupSettings(
            autoBackup = p[Keys.BACKUP_AUTO] ?: false,
            network = p[Keys.BACKUP_NETWORK].toEnum(BackupNetwork.WIFI_ONLY),
            accountEmail = p[Keys.BACKUP_EMAIL],
            accountName = p[Keys.BACKUP_NAME],
            lastBackupAt = p[Keys.BACKUP_LAST],
        )
    }.distinctUntilChanged()

    suspend fun appLockPatternHash(): String? = data.first()[Keys.APP_LOCK_PATTERN]

    suspend fun update(block: MutablePrefs.() -> Unit) {
        store.edit { MutablePrefs(it).block() }
    }

    /** Typed setters so callers never touch raw keys. */
    class MutablePrefs internal constructor(private val p: androidx.datastore.preferences.core.MutablePreferences) {
        var themeMode: ThemeMode by enumPref(Keys.THEME, ThemeMode.SYSTEM)
        var appIcon: AppIconOption by enumPref(Keys.APP_ICON, AppIconOption.DEFAULT)
        var gridSize: GridSize by enumPref(Keys.GRID_SIZE, GridSize.MEDIUM)
        var lockAfter: LockAfter by enumPref(Keys.APP_LOCK_AFTER, LockAfter.IMMEDIATELY)
        var appLockMethod: AppLockAuthMethod by enumPref(Keys.APP_LOCK_METHOD, AppLockAuthMethod.PIN)
        var backupNetwork: BackupNetwork by enumPref(Keys.BACKUP_NETWORK, BackupNetwork.WIFI_ONLY)

        fun setLanguage(tag: String?) {
            if (tag == null) p.remove(Keys.LANGUAGE) else p[Keys.LANGUAGE] = tag
            p[Keys.LANGUAGE_CHOSEN] = true
        }

        fun set(key: BoolKey, value: Boolean) { p[key.key] = value }
        fun setSlideshowInterval(seconds: Int) { p[Keys.SLIDESHOW_INTERVAL] = seconds }
        fun setIntruderAttempts(count: Int) {
            p[Keys.INTRUDER_ATTEMPTS] = count.coerceIn(VaultConstants.MIN_INTRUDER_ATTEMPTS, VaultConstants.MAX_INTRUDER_ATTEMPTS)
        }
        fun setLockedPackages(packages: Set<String>) { p[Keys.APP_LOCK_PACKAGES] = packages }
        fun lockedPackages(): Set<String> = p[Keys.APP_LOCK_PACKAGES] ?: emptySet()
        fun setAppLockPatternHash(hash: String?) {
            if (hash == null) p.remove(Keys.APP_LOCK_PATTERN) else p[Keys.APP_LOCK_PATTERN] = hash
        }
        fun setBackupAccount(email: String?, name: String?) {
            if (email == null) p.remove(Keys.BACKUP_EMAIL) else p[Keys.BACKUP_EMAIL] = email
            if (name == null) p.remove(Keys.BACKUP_NAME) else p[Keys.BACKUP_NAME] = name
        }
        fun setLastBackup(at: Long) { p[Keys.BACKUP_LAST] = at }

        private fun <E : Enum<E>> enumPref(key: Preferences.Key<String>, default: E) =
            object : kotlin.properties.ReadWriteProperty<Any?, E> {
                override fun getValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>): E =
                    p[key]?.let { name -> default.declaringJavaClass.enumConstants?.firstOrNull { it.name == name } } ?: default

                override fun setValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>, value: E) {
                    p[key] = value.name
                }
            }
    }

    enum class BoolKey(internal val key: Preferences.Key<Boolean>) {
        WELCOME_ACCEPTED(Keys.WELCOME_ACCEPTED),
        ONBOARDING_DONE(Keys.ONBOARDING_DONE),
        RECOVERY_PROMPT_HANDLED(Keys.RECOVERY_PROMPT_HANDLED),
        PANIC(Keys.PANIC),
        INTRUDER(Keys.INTRUDER),
        INTRUDER_SELFIE(Keys.INTRUDER_SELFIE),
        PREVENT_SCREENSHOTS(Keys.PREVENT_SCREENSHOTS),
        FINGERPRINT_UNLOCK(Keys.FINGERPRINT_UNLOCK),
        NOTIFICATIONS(Keys.NOTIFICATIONS),
        CLOUD_BANNER_DISMISSED(Keys.CLOUD_BANNER_DISMISSED),
        RECOVERY_BANNER_DISMISSED(Keys.RECOVERY_BANNER_DISMISSED),
        RESET_CODE_BANNER_DISMISSED(Keys.RESET_CODE_BANNER_DISMISSED),
        GROUP_BY_DATE(Keys.GROUP_BY_DATE),
        APP_LOCK_ENABLED(Keys.APP_LOCK_ENABLED),
        BACKUP_AUTO(Keys.BACKUP_AUTO),
    }

    private object Keys {
        val THEME = stringPreferencesKey("theme_mode")
        val LANGUAGE = stringPreferencesKey("language_tag")
        val LANGUAGE_CHOSEN = booleanPreferencesKey("language_chosen")
        val WELCOME_ACCEPTED = booleanPreferencesKey("welcome_accepted")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_completed")
        val RECOVERY_PROMPT_HANDLED = booleanPreferencesKey("recovery_prompt_handled")
        val APP_ICON = stringPreferencesKey("app_icon")
        val PANIC = booleanPreferencesKey("panic_mode")
        val INTRUDER = booleanPreferencesKey("intruder_detection")
        val INTRUDER_SELFIE = booleanPreferencesKey("intruder_selfie")
        val INTRUDER_ATTEMPTS = intPreferencesKey("intruder_attempts")
        val PREVENT_SCREENSHOTS = booleanPreferencesKey("prevent_screenshots")
        val FINGERPRINT_UNLOCK = booleanPreferencesKey("fingerprint_unlock")
        val NOTIFICATIONS = booleanPreferencesKey("notifications")
        val CLOUD_BANNER_DISMISSED = booleanPreferencesKey("cloud_banner_dismissed")
        val RECOVERY_BANNER_DISMISSED = booleanPreferencesKey("recovery_banner_dismissed")
        val RESET_CODE_BANNER_DISMISSED = booleanPreferencesKey("reset_code_banner_dismissed")
        val GRID_SIZE = stringPreferencesKey("grid_size")
        val GROUP_BY_DATE = booleanPreferencesKey("group_by_date")
        val SLIDESHOW_INTERVAL = intPreferencesKey("slideshow_interval")
        val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        val APP_LOCK_AFTER = stringPreferencesKey("app_lock_after")
        val APP_LOCK_METHOD = stringPreferencesKey("app_lock_method")
        val APP_LOCK_PACKAGES = stringSetPreferencesKey("app_lock_packages")
        val APP_LOCK_PATTERN = stringPreferencesKey("app_lock_pattern")
        val BACKUP_AUTO = booleanPreferencesKey("backup_auto")
        val BACKUP_NETWORK = stringPreferencesKey("backup_network")
        val BACKUP_EMAIL = stringPreferencesKey("backup_email")
        val BACKUP_NAME = stringPreferencesKey("backup_name")
        val BACKUP_LAST = longPreferencesKey("backup_last")
    }

    companion object {
        const val FILE_NAME = "vault_settings"
    }
}

private inline fun <reified E : Enum<E>> String?.toEnum(default: E): E =
    this?.let { name -> enumValues<E>().firstOrNull { it.name == name } } ?: default
