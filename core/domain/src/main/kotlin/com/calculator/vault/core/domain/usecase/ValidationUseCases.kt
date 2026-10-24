package com.calculator.vault.core.domain.usecase

import com.calculator.vault.core.model.PinCheckResult
import org.koin.core.annotation.Factory

object PinRules {
    const val LENGTH = 4

    fun isValid(pin: String): Boolean = pin.length == LENGTH && pin.all(Char::isDigit)
}

@Factory
class ValidateEmailUseCase {
    operator fun invoke(email: String): Boolean = EMAIL.matches(email.trim())

    private companion object {
        val EMAIL = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }
}

/** Unlock attempt from the calculator: anything that isn't exactly 4 digits is just maths. */
@Factory
class UnlockWithPinUseCase(private val auth: com.calculator.vault.core.domain.repository.AuthRepository) {
    suspend operator fun invoke(input: String): PinCheckResult =
        if (!PinRules.isValid(input)) PinCheckResult.NotAPin else auth.unlockWithPin(input)
}
