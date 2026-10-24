package com.calculator.vault.core.security.session

import com.calculator.vault.core.security.crypto.MasterKeys
import com.calculator.vault.core.security.crypto.VaultCipher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.annotation.Single
import timber.log.Timber
import java.util.concurrent.atomic.AtomicInteger

class VaultLockedException : IllegalStateException("The vault is locked")

/**
 * Holds the decrypted keys while the vault is unlocked.
 *
 * Locking hides the UI immediately, but long-running jobs (an import that is still encrypting
 * files) hold a lease so their keys are wiped only once they finish.
 */
@Single
class SessionKeys {
    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    @Volatile private var cipher: VaultCipher? = null
    private val leases = AtomicInteger(0)
    @Volatile private var lockRequested = false

    fun open(keys: MasterKeys) {
        cipher = VaultCipher(keys)
        lockRequested = false
        _isUnlocked.value = true
    }

    fun lock() {
        _isUnlocked.value = false
        lockRequested = true
        wipeIfIdle()
    }

    fun cipherOrNull(): VaultCipher? = cipher

    fun require(): VaultCipher = cipher ?: throw VaultLockedException()

    /** Runs [block] with the keys, keeping them alive even if the user locks meanwhile. */
    suspend fun <T> withKeys(block: suspend (VaultCipher) -> T): T {
        val current = require()
        leases.incrementAndGet()
        try {
            return block(current)
        } finally {
            leases.decrementAndGet()
            wipeIfIdle()
        }
    }

    private fun wipeIfIdle() {
        if (lockRequested && leases.get() == 0 && cipher != null) {
            cipher = null
            Timber.d("Session keys wiped")
        }
    }
}
