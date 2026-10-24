package com.calculator.vault.feature.pin.domain.usecase

import com.calculator.vault.core.domain.repository.AuthRepository
import com.calculator.vault.core.domain.usecase.PinRules
import org.koin.core.annotation.Factory

@Factory
class CreateVaultUseCase(private val auth: AuthRepository) {
    suspend operator fun invoke(pin: String): Result<Unit> {
        require(PinRules.isValid(pin))
        return auth.createVault(pin)
    }
}

@Factory
class VerifyCurrentPinUseCase(private val auth: AuthRepository) {
    suspend operator fun invoke(pin: String): Boolean = auth.verifyPin(pin)
}

@Factory
class ChangePinUseCase(private val auth: AuthRepository) {
    suspend operator fun invoke(current: String, new: String): Result<Unit> = auth.changePin(current, new)
}

/** Sets a new PIN after recovery unlocked the vault. */
@Factory
class ResetPinUseCase(private val auth: AuthRepository) {
    suspend operator fun invoke(new: String): Result<Unit> = auth.resetPin(new)
}
