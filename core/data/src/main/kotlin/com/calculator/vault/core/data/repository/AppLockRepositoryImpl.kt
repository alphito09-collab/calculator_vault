package com.calculator.vault.core.data.repository

import android.os.SystemClock
import com.calculator.vault.core.common.dispatchers.VaultDispatchers
import com.calculator.vault.core.datastore.VaultPreferences
import com.calculator.vault.core.datastore.VaultPreferences.BoolKey
import com.calculator.vault.core.domain.repository.AppLockRepository
import com.calculator.vault.core.model.AppLockAuthMethod
import com.calculator.vault.core.model.AppLockSettings
import com.calculator.vault.core.model.LockAfter
import com.calculator.vault.core.security.crypto.SecretWrapping
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import java.security.MessageDigest
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap

@Single(binds = [AppLockRepository::class])
class AppLockRepositoryImpl(
    private val prefs: VaultPreferences,
    private val dispatchers: VaultDispatchers,
) : AppLockRepository {

    /** packageName → elapsedRealtime when it was last unlocked. */
    private val unlockedAt = ConcurrentHashMap<String, Long>()

    override val settings: Flow<AppLockSettings> = prefs.appLock

    override suspend fun setEnabled(enabled: Boolean) = prefs.update { set(BoolKey.APP_LOCK_ENABLED, enabled) }
    override suspend fun setLockAfter(lockAfter: LockAfter) = prefs.update { this.lockAfter = lockAfter }
    override suspend fun setAuthMethod(method: AppLockAuthMethod) = prefs.update { appLockMethod = method }

    override suspend fun setLocked(packageName: String, locked: Boolean) = prefs.update {
        setLockedPackages(if (locked) lockedPackages() + packageName else lockedPackages() - packageName)
    }

    override suspend fun setAppLockPattern(pattern: List<Int>) {
        val hash = hashPattern(pattern, SecretWrapping.newSalt())
        prefs.update { setAppLockPatternHash(hash) }
    }

    override suspend fun verifyAppLockPattern(pattern: List<Int>): Boolean {
        val stored = prefs.appLockPatternHash() ?: return false
        val salt = Base64.getDecoder().decode(stored.substringBefore(':'))
        return MessageDigest.isEqual(hashPattern(pattern, salt).toByteArray(), stored.toByteArray())
    }

    private suspend fun hashPattern(pattern: List<Int>, salt: ByteArray): String = withContext(dispatchers.default) {
        val key = SecretWrapping.deriveKey(SecretWrapping.patternSecret(pattern), salt, 20_000)
        Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(key)
    }

    override fun markUnlocked(packageName: String) {
        unlockedAt[packageName] = SystemClock.elapsedRealtime()
    }

    override fun isTemporarilyUnlocked(packageName: String, lockAfter: LockAfter): Boolean {
        val at = unlockedAt[packageName] ?: return false
        return SystemClock.elapsedRealtime() - at < lockAfter.millis.coerceAtLeast(GRACE_FOR_IMMEDIATE_MS)
    }

    override fun relockAll() = unlockedAt.clear()

    private companion object {
        /** "Immediately" still tolerates the app switch that triggered the unlock screen itself. */
        const val GRACE_FOR_IMMEDIATE_MS = 1_500L
    }
}
