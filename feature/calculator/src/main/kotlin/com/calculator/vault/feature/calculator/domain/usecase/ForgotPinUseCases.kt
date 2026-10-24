package com.calculator.vault.feature.calculator.domain.usecase

import com.calculator.vault.core.common.VaultConstants
import com.calculator.vault.core.domain.repository.RecoveryRepository
import com.calculator.vault.core.domain.repository.SettingsRepository
import com.calculator.vault.core.model.RecoveryMethod
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory

/**
 * Decides when the calculator offers "Forgot PIN?". It needs at least one recovery method
 * (without one the reset code is the only way back), and waits for
 * - intruder limit + 2 wrong PINs while intruder detection is on, so the intruder is logged and
 *   photographed before any way around the PIN is shown;
 * - 3 wrong PINs otherwise.
 */
@Factory
class ForgotPinPromptUseCase(
    private val settings: SettingsRepository,
    private val recovery: RecoveryRepository,
) {
    /** The methods to offer after [consecutiveFailures] wrong PINs, or empty for no dialog. */
    suspend operator fun invoke(consecutiveFailures: Int): List<RecoveryMethod> {
        val s = settings.settings.first()
        val threshold = if (s.intruderDetectionEnabled) {
            s.intruderAttempts + VaultConstants.FORGOT_PIN_ATTEMPTS_AFTER_INTRUDER
        } else {
            VaultConstants.FORGOT_PIN_ATTEMPTS
        }
        return if (consecutiveFailures >= threshold) recovery.availableMethods() else emptyList()
    }

    /** Every usable method, for long-pressing "=". */
    suspend fun anyTime(): List<RecoveryMethod> = recovery.availableMethods()
}

/** "7860786 =" on the calculator: opens the vault on this install so a new PIN can be set. */
@Factory
class ResetPinWithCodeUseCase(private val recovery: RecoveryRepository) {
    suspend operator fun invoke(): Result<Unit> = recovery.unlockWithResetCode()
}
