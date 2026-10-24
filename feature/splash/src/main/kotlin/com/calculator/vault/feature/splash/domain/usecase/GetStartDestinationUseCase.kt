package com.calculator.vault.feature.splash.domain.usecase

import com.calculator.vault.core.domain.repository.AuthRepository
import com.calculator.vault.core.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory

enum class StartDestination { WELCOME, LANGUAGE, ONBOARDING, CREATE_PIN, CALCULATOR }

/**
 * First install: the splash itself is the "Get Started" welcome, then language → onboarding →
 * PIN, resuming at the first unfinished step. Once a vault exists it is always the calculator.
 */
@Factory
class GetStartDestinationUseCase(
    private val settings: SettingsRepository,
    private val auth: AuthRepository,
) {
    suspend operator fun invoke(): StartDestination {
        if (auth.isVaultCreated.first()) return StartDestination.CALCULATOR
        val s = settings.settings.first()
        return when {
            // Installs from before the welcome step already passed it.
            !s.welcomeAccepted && !s.languageChosen && !s.onboardingCompleted -> StartDestination.WELCOME
            !s.languageChosen -> StartDestination.LANGUAGE
            !s.onboardingCompleted -> StartDestination.ONBOARDING
            else -> StartDestination.CREATE_PIN
        }
    }
}

/** "Get Started" accepts the Privacy Policy and Terms of Use. */
@Factory
class AcceptWelcomeUseCase(private val settings: SettingsRepository) {
    suspend operator fun invoke() = settings.setWelcomeAccepted()
}
