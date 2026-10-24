package com.calculator.vault.core.domain.repository

import com.calculator.vault.core.model.RecoveryMethod
import com.calculator.vault.core.model.RecoveryOptions
import com.calculator.vault.core.model.SecurityQuestion
import kotlinx.coroutines.flow.Flow

/**
 * Recovery methods. Pattern and security answer wrap the vault key, so they keep working
 * after a reinstall; email recovery relies on a device-bound key and our OTP backend.
 */
interface RecoveryRepository {
    val options: Flow<RecoveryOptions>

    /** Methods usable from the "forgot PIN" flow right now. */
    suspend fun availableMethods(): List<RecoveryMethod>

    suspend fun setPattern(pattern: List<Int>): Result<Unit>
    suspend fun clearPattern(): Result<Unit>
    suspend fun unlockWithPattern(pattern: List<Int>): Result<Unit>
    suspend fun verifyPattern(pattern: List<Int>): Boolean

    suspend fun setSecurityAnswer(question: SecurityQuestion, answer: String): Result<Unit>
    suspend fun unlockWithSecurityAnswer(answer: String): Result<Unit>

    /** Stores a verified recovery email. */
    suspend fun setRecoveryEmail(email: String): Result<Unit>
    suspend fun unlockWithVerifiedEmail(): Result<Unit>

    /**
     * Opens the vault for the reset code ([com.calculator.vault.core.common.VaultConstants.RESET_PIN_CODE])
     * using the device-bound key copy, so it fails after a reinstall.
     */
    suspend fun unlockWithResetCode(): Result<Unit>

    suspend fun setFingerprintRecovery(enabled: Boolean)
}
