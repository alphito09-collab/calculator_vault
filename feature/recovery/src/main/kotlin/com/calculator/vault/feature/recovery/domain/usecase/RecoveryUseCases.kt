package com.calculator.vault.feature.recovery.domain.usecase

import com.calculator.vault.core.domain.repository.AppLockRepository
import com.calculator.vault.core.domain.repository.AuthRepository
import com.calculator.vault.core.domain.repository.NetworkMonitor
import com.calculator.vault.core.domain.repository.OtpRepository
import com.calculator.vault.core.domain.repository.RecoveryRepository
import com.calculator.vault.core.domain.repository.SettingsRepository
import com.calculator.vault.core.model.RecoveryMethod
import com.calculator.vault.core.model.RecoveryOptions
import com.calculator.vault.core.model.SecurityQuestion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory
import javax.crypto.Cipher

@Factory
class ObserveRecoveryOptionsUseCase(private val recovery: RecoveryRepository) {
    operator fun invoke(): Flow<RecoveryOptions> = recovery.options
}

@Factory
class AvailableRecoveryMethodsUseCase(private val recovery: RecoveryRepository) {
    suspend operator fun invoke(): List<RecoveryMethod> = recovery.availableMethods()
}

@Factory
class FingerprintRecoveryUseCase(private val auth: AuthRepository) {
    fun enrollCipher(): Cipher? = auth.biometricEnrollCipher()
    suspend fun enable(cipher: Cipher) = auth.enableBiometricUnlock(cipher)
    suspend fun disable() = auth.disableBiometricUnlock()
    suspend fun unlockCipher(): Cipher? = auth.biometricUnlockCipher()
    suspend fun unlock(cipher: Cipher) = auth.unlockWithBiometric(cipher)
}

enum class PatternTarget { RECOVERY, APP_LOCK }

@Factory
class SavePatternUseCase(private val recovery: RecoveryRepository, private val appLock: AppLockRepository) {
    suspend operator fun invoke(target: PatternTarget, pattern: List<Int>): Result<Unit> = when (target) {
        PatternTarget.RECOVERY -> recovery.setPattern(pattern)
        PatternTarget.APP_LOCK -> runCatching { appLock.setAppLockPattern(pattern) }
    }
}

@Factory
class UnlockWithPatternUseCase(private val recovery: RecoveryRepository) {
    suspend operator fun invoke(pattern: List<Int>): Result<Unit> = recovery.unlockWithPattern(pattern)
}

@Factory
class SaveSecurityAnswerUseCase(private val recovery: RecoveryRepository) {
    suspend operator fun invoke(question: SecurityQuestion, answer: String) = recovery.setSecurityAnswer(question, answer)
}

@Factory
class UnlockWithAnswerUseCase(private val recovery: RecoveryRepository) {
    suspend operator fun invoke(answer: String) = recovery.unlockWithSecurityAnswer(answer)
}

@Factory
class SendOtpUseCase(private val otp: OtpRepository, private val network: NetworkMonitor) {
    sealed interface Outcome {
        data object Sent : Outcome
        data object Offline : Outcome
        data class Failed(val error: Throwable) : Outcome
    }

    suspend operator fun invoke(email: String): Outcome {
        if (!network.isOnline.first()) return Outcome.Offline
        return otp.sendCode(email).fold({ Outcome.Sent }, { Outcome.Failed(it) })
    }
}

@Factory
class VerifyOtpUseCase(private val otp: OtpRepository, private val network: NetworkMonitor) {
    suspend operator fun invoke(email: String, code: String): Result<Boolean> {
        if (!network.isOnline.first()) return Result.failure(IllegalStateException("offline"))
        return otp.verifyCode(email, code)
    }
}

@Factory
class CompleteEmailRecoveryUseCase(private val recovery: RecoveryRepository) {
    suspend fun saveEmail(email: String) = recovery.setRecoveryEmail(email)
    suspend fun unlock() = recovery.unlockWithVerifiedEmail()
}

@Factory
class FinishRecoveryPromptUseCase(private val settings: SettingsRepository) {
    suspend operator fun invoke() = settings.setRecoveryPromptHandled()
}
