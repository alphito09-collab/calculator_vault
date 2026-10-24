package com.calculator.vault.core.domain.usecase

import com.calculator.vault.core.domain.repository.VaultRepository
import com.calculator.vault.core.model.DeviceMedia
import com.calculator.vault.core.model.MediaType
import com.calculator.vault.core.model.TransferProgress
import com.calculator.vault.core.model.VaultCounts
import com.calculator.vault.core.model.VaultItem
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory
import java.io.File

@Factory
class ObserveVaultItemsUseCase(private val repository: VaultRepository) {
    operator fun invoke(type: MediaType): Flow<List<VaultItem>> = repository.observeItems(type)
}

@Factory
class ObserveVaultItemUseCase(private val repository: VaultRepository) {
    operator fun invoke(id: String): Flow<VaultItem?> = repository.observeItem(id)
}

@Factory
class ObserveVaultCountsUseCase(private val repository: VaultRepository) {
    operator fun invoke(): Flow<VaultCounts> = repository.observeCounts()
}

@Factory
class ObserveTrashUseCase(private val repository: VaultRepository) {
    operator fun invoke(): Flow<List<VaultItem>> = repository.observeTrash()
}

@Factory
class HideMediaUseCase(private val repository: VaultRepository) {
    operator fun invoke(sources: List<DeviceMedia>): Flow<TransferProgress> = repository.hide(sources, deleteOriginals = true)
}

@Factory
class UnhideItemsUseCase(private val repository: VaultRepository) {
    operator fun invoke(ids: List<String>): Flow<TransferProgress> = repository.unhide(ids)
}

@Factory
class MoveToTrashUseCase(private val repository: VaultRepository) {
    suspend operator fun invoke(ids: List<String>): Result<Unit> = repository.moveToTrash(ids)
}

@Factory
class RestoreFromTrashUseCase(private val repository: VaultRepository) {
    suspend operator fun invoke(ids: List<String>): Result<Unit> = repository.restoreFromTrash(ids)
}

@Factory
class DeletePermanentlyUseCase(private val repository: VaultRepository) {
    suspend operator fun invoke(ids: List<String>): Result<Unit> = repository.deletePermanently(ids)
}

@Factory
class EmptyTrashUseCase(private val repository: VaultRepository) {
    suspend operator fun invoke(): Result<Unit> = repository.emptyTrash()
}

@Factory
class DecryptForSharingUseCase(private val repository: VaultRepository) {
    suspend operator fun invoke(ids: List<String>): Result<List<File>> = repository.decryptToCache(ids)
}

@Factory
class AddCaptureUseCase(private val repository: VaultRepository) {
    suspend operator fun invoke(file: File, type: MediaType, mimeType: String): Result<VaultItem> =
        repository.addCapture(file, type, mimeType)
}

/**
 * Renames an item while keeping its extension, so "IMG_1.jpg" renamed to "Beach" becomes
 * "Beach.jpg". Rejects blank names and path separators.
 */
@Factory
class RenameItemUseCase(private val repository: VaultRepository) {
    suspend operator fun invoke(item: VaultItem, input: String): Result<Unit> {
        val name = normalizedName(item.name, input) ?: return Result.failure(IllegalArgumentException("Invalid name"))
        if (name == item.name) return Result.success(Unit)
        return repository.rename(item.id, name)
    }

    companion object {
        private val ILLEGAL = Regex("[\\\\/:*?\"<>|]")

        fun normalizedName(current: String, input: String): String? {
            val trimmed = input.trim()
            if (trimmed.isEmpty() || ILLEGAL.containsMatchIn(trimmed) || trimmed.length > 120) return null
            val ext = current.substringAfterLast('.', missingDelimiterValue = "")
            return if (ext.isEmpty() || trimmed.endsWith(".$ext", ignoreCase = true)) trimmed else "$trimmed.$ext"
        }
    }
}
