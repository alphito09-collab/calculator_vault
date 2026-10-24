package com.calculator.vault.core.domain.repository

import com.calculator.vault.core.model.DeviceMedia
import com.calculator.vault.core.model.MediaType
import com.calculator.vault.core.model.TransferProgress
import com.calculator.vault.core.model.VaultCounts
import com.calculator.vault.core.model.VaultItem
import kotlinx.coroutines.flow.Flow
import java.io.File

/** Encrypted vault content. All operations require an unlocked session. */
interface VaultRepository {
    fun observeItems(type: MediaType): Flow<List<VaultItem>>
    fun observeTrash(): Flow<List<VaultItem>>
    fun observeCounts(): Flow<VaultCounts>
    fun observeItem(id: String): Flow<VaultItem?>

    suspend fun getItem(id: String): VaultItem?

    /** Encrypts [sources] into the vault and (optionally) removes the originals. */
    fun hide(sources: List<DeviceMedia>, deleteOriginals: Boolean = true): Flow<TransferProgress>

    /** Encrypts a file captured by the in-app camera, then deletes the plaintext temp file. */
    suspend fun addCapture(file: File, type: MediaType, mimeType: String): Result<VaultItem>

    /** Decrypts items back to public storage and removes them from the vault. */
    fun unhide(ids: List<String>): Flow<TransferProgress>

    suspend fun rename(id: String, newName: String): Result<Unit>
    suspend fun moveToTrash(ids: List<String>): Result<Unit>
    suspend fun restoreFromTrash(ids: List<String>): Result<Unit>
    suspend fun deletePermanently(ids: List<String>): Result<Unit>
    suspend fun emptyTrash(): Result<Unit>

    /** Plaintext copies in the private cache for sharing/viewing; wiped on lock. */
    suspend fun decryptToCache(ids: List<String>): Result<List<File>>

    /** Rebuilds the local index from the encrypted folder (after reinstall or restore). */
    suspend fun rebuildIndexIfNeeded(): Result<Int>
}
