package com.calculator.vault.core.domain.repository

import com.calculator.vault.core.model.PinCheckResult
import kotlinx.coroutines.flow.Flow
import javax.crypto.Cipher

/** PIN lifecycle and the unlocked session. */
interface AuthRepository {
    /** True once a vault (PIN + keys) exists on this install. */
    val isVaultCreated: Flow<Boolean>

    val isUnlocked: Flow<Boolean>

    fun isUnlockedNow(): Boolean

    /** Creates a brand-new vault protected by [pin] and unlocks it. */
    suspend fun createVault(pin: String): Result<Unit>

    /** Tries to unlock with [pin]. Wrong PINs are counted for intruder detection. */
    suspend fun unlockWithPin(pin: String): PinCheckResult

    /** Verifies the PIN without changing session state (App Lock, change-PIN flow). */
    suspend fun verifyPin(pin: String): Boolean

    suspend fun changePin(currentPin: String, newPin: String): Result<Unit>

    /** Sets a new PIN while unlocked (after recovery succeeded). */
    suspend fun resetPin(newPin: String): Result<Unit>

    fun lock()

    // region Biometrics
    val isBiometricUnlockEnabled: Flow<Boolean>

    /** Cipher to hand to BiometricPrompt when enabling fingerprint unlock. */
    fun biometricEnrollCipher(): Cipher?

    suspend fun enableBiometricUnlock(authenticatedCipher: Cipher): Result<Unit>

    /** Cipher to hand to BiometricPrompt when unlocking; null if keys were invalidated. */
    suspend fun biometricUnlockCipher(): Cipher?

    suspend fun unlockWithBiometric(authenticatedCipher: Cipher): Result<Unit>

    suspend fun disableBiometricUnlock()
    // endregion
}
