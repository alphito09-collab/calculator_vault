package com.calculator.vault.core.domain.repository

import com.calculator.vault.core.model.AppLockAuthMethod
import com.calculator.vault.core.model.AppLockSettings
import com.calculator.vault.core.model.LockAfter
import kotlinx.coroutines.flow.Flow

/** Locking *other* apps behind the vault credentials. */
interface AppLockRepository {
    val settings: Flow<AppLockSettings>

    suspend fun setEnabled(enabled: Boolean)
    suspend fun setLockAfter(lockAfter: LockAfter)
    suspend fun setAuthMethod(method: AppLockAuthMethod)
    suspend fun setLocked(packageName: String, locked: Boolean)
    suspend fun setAppLockPattern(pattern: List<Int>)
    suspend fun verifyAppLockPattern(pattern: List<Int>): Boolean

    /** Grace period bookkeeping for "Lock after". */
    fun markUnlocked(packageName: String)
    fun isTemporarilyUnlocked(packageName: String, lockAfter: LockAfter): Boolean
    fun relockAll()
}
