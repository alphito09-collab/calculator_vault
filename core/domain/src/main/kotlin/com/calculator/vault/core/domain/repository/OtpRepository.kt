package com.calculator.vault.core.domain.repository

/** One-time codes sent by our own backend (see core:network OtpApi). */
interface OtpRepository {
    suspend fun sendCode(email: String): Result<Unit>
    suspend fun verifyCode(email: String, code: String): Result<Boolean>
}
