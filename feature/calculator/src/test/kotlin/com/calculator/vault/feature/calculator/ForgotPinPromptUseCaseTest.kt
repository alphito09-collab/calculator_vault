package com.calculator.vault.feature.calculator

import com.calculator.vault.core.domain.repository.RecoveryRepository
import com.calculator.vault.core.domain.repository.SettingsRepository
import com.calculator.vault.core.model.RecoveryMethod
import com.calculator.vault.core.model.UserSettings
import com.calculator.vault.feature.calculator.domain.usecase.ForgotPinPromptUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ForgotPinPromptUseCaseTest {

    private fun useCase(settings: UserSettings, methods: List<RecoveryMethod>) = ForgotPinPromptUseCase(
        settings = mockk<SettingsRepository> { every { this@mockk.settings } returns flowOf(settings) },
        recovery = mockk<RecoveryRepository> { coEvery { availableMethods() } returns methods },
    )

    private val methods = listOf(RecoveryMethod.PATTERN, RecoveryMethod.SECURITY_QUESTION)

    @Test fun intruderOnWaitsTwoPastTheIntruderLimit() = runTest {
        val prompt = useCase(UserSettings(intruderDetectionEnabled = true, intruderAttempts = 5), methods)
        assertTrue(prompt(6).isEmpty())
        assertEquals(methods, prompt(7))
        assertEquals(methods, prompt(8))
    }

    @Test fun intruderOffOffersAfterThree() = runTest {
        val prompt = useCase(UserSettings(intruderDetectionEnabled = false, intruderAttempts = 5), methods)
        assertTrue(prompt(2).isEmpty())
        assertEquals(methods, prompt(3))
    }

    @Test fun noRecoveryMethodNoDialog() = runTest {
        val prompt = useCase(UserSettings(intruderDetectionEnabled = false), emptyList())
        assertTrue(prompt(10).isEmpty())
    }
}
