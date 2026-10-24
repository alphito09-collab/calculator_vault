package com.calculator.vault.core.domain.repository

/** Access to the hidden folder in shared storage that survives uninstall. */
interface StorageRepository {
    fun hasAllFilesAccess(): Boolean

    /** A vault created by a previous install was found in shared storage. */
    suspend fun findPreviousVault(): PreviousVault?

    /** Adopts the previous vault: [pin] must be the PIN used on that install. */
    suspend fun adoptPreviousVault(pin: String): Result<Unit>

    /** Moves the previous vault aside so the current one can be used. */
    suspend fun archivePreviousVault(): Result<Unit>

    /** Copies the current install's vault config into shared storage. */
    suspend fun syncConfigToSharedStorage(): Result<Unit>

    data class PreviousVault(val createdAt: Long, val itemCount: Int)
}
