package com.calculator.vault.feature.intruder.domain.usecase

import com.calculator.vault.core.domain.repository.IntruderCamera
import com.calculator.vault.core.domain.repository.IntruderRepository
import com.calculator.vault.core.domain.repository.SettingsRepository
import com.calculator.vault.core.model.IntruderAttempt
import com.calculator.vault.core.model.IntruderStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.koin.core.annotation.Factory

data class IntruderOverview(
    val detectionEnabled: Boolean,
    val selfieEnabled: Boolean,
    val attemptLimit: Int,
    val stats: IntruderStats,
    val attempts: List<IntruderAttempt>,
)

@Factory
class ObserveIntruderOverviewUseCase(
    private val settings: SettingsRepository,
    private val intruders: IntruderRepository,
) {
    operator fun invoke(): Flow<IntruderOverview> =
        combine(settings.settings, intruders.stats, intruders.attempts) { s, stats, attempts ->
            IntruderOverview(s.intruderDetectionEnabled, s.intruderSelfieEnabled, s.intruderAttempts, stats, attempts)
        }
}

@Factory
class IntruderSettingsUseCase(
    private val settings: SettingsRepository,
    private val intruders: IntruderRepository,
    private val camera: IntruderCamera,
) {
    suspend fun setDetection(enabled: Boolean) = settings.setIntruderDetection(enabled)
    suspend fun setSelfie(enabled: Boolean) = settings.setIntruderSelfie(enabled)
    suspend fun setAttemptLimit(count: Int) = settings.setIntruderAttempts(count)
    fun hasCameraPermission() = camera.hasPermission()
    suspend fun clear() = intruders.clear()
}
