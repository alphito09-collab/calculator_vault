package com.calculator.vault.feature.calculator.domain.usecase

import com.calculator.vault.core.domain.repository.IntruderCamera
import com.calculator.vault.core.domain.repository.IntruderRepository
import com.calculator.vault.core.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory

/**
 * Logs a failed unlock once the streak reaches the user's intruder limit (1–6 wrong PINs);
 * earlier misses are treated as typos. At the limit a front-camera photo is taken (once per
 * burst) when "Intruder selfie" is on and the camera permission was granted, and every later
 * miss updates the logged count.
 */
@Factory
class RecordIntruderUseCase(
    private val settings: SettingsRepository,
    private val intruders: IntruderRepository,
    private val camera: IntruderCamera,
) {
    suspend operator fun invoke(burstId: String, consecutiveFailures: Int, photoAlreadyTaken: Boolean): Boolean {
        val s = settings.settings.first()
        if (!s.intruderDetectionEnabled || consecutiveFailures < s.intruderAttempts) return photoAlreadyTaken
        val shouldCapture = !photoAlreadyTaken && s.intruderSelfieEnabled && camera.hasPermission()
        val photo = if (shouldCapture) camera.captureSelfie() else null
        intruders.recordFailure(burstId, consecutiveFailures, photo)
        return photoAlreadyTaken || photo != null
    }
}
