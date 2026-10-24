package com.calculator.vault.core.domain.repository

import com.calculator.vault.core.model.IntruderAttempt
import com.calculator.vault.core.model.IntruderStats
import kotlinx.coroutines.flow.Flow

interface IntruderRepository {
    val attempts: Flow<List<IntruderAttempt>>
    val stats: Flow<IntruderStats>

    /**
     * Records the running count of a burst of failed unlocks. Calls with the same [burstId]
     * update one entry; [photoJpeg] (encrypted with the vault public key) is attached once.
     */
    suspend fun recordFailure(burstId: String, failedAttempts: Int, photoJpeg: ByteArray?)

    suspend fun clear()
}
