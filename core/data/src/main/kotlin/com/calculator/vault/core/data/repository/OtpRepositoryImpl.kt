package com.calculator.vault.core.data.repository

import com.calculator.vault.core.common.result.runSuspendCatching
import com.calculator.vault.core.domain.repository.OtpRepository
import com.calculator.vault.core.network.otp.OtpApi
import org.koin.core.annotation.Single

@Single(binds = [OtpRepository::class])
class OtpRepositoryImpl(private val api: OtpApi) : OtpRepository {
    override suspend fun sendCode(email: String): Result<Unit> = runSuspendCatching { api.send(email.trim()) }

    override suspend fun verifyCode(email: String, code: String): Result<Boolean> =
        runSuspendCatching { api.verify(email.trim(), code.trim()) }
}
