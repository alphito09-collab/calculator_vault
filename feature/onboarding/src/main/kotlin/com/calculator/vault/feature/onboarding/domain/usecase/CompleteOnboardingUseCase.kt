package com.calculator.vault.feature.onboarding.domain.usecase

import com.calculator.vault.core.domain.repository.SettingsRepository
import org.koin.core.annotation.Factory

@Factory
class CompleteOnboardingUseCase(private val settings: SettingsRepository) {
    suspend operator fun invoke() = settings.setOnboardingCompleted()
}
