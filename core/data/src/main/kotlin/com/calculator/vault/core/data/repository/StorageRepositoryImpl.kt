package com.calculator.vault.core.data.repository

import com.calculator.vault.core.common.dispatchers.VaultDispatchers
import com.calculator.vault.core.common.result.runSuspendCatching
import com.calculator.vault.core.data.config.VaultConfigStore
import com.calculator.vault.core.data.storage.StorageAccess
import com.calculator.vault.core.data.storage.VaultPaths
import com.calculator.vault.core.database.dao.VaultItemDao
import com.calculator.vault.core.domain.repository.StorageRepository
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import java.io.File

@Single(binds = [StorageRepository::class])
class StorageRepositoryImpl(
    private val access: StorageAccess,
    private val paths: VaultPaths,
    private val configStore: VaultConfigStore,
    private val keyring: VaultKeyring,
    private val auth: AuthRepositoryImpl,
    private val dao: VaultItemDao,
    private val dispatchers: VaultDispatchers,
) : StorageRepository {

    override fun hasAllFilesAccess(): Boolean = access.hasAllFilesAccess()

    override suspend fun findPreviousVault(): StorageRepository.PreviousVault? = withContext(dispatchers.io) {
        if (!access.hasAllFilesAccess()) return@withContext null
        val shared = configStore.readShared() ?: return@withContext null
        val current = configStore.current()
        if (current != null && current.vaultId == shared.vaultId) return@withContext null
        val count = File(paths.sharedRoot, "items").listFiles { f -> f.name.endsWith(VaultPaths.META_EXT) }?.size ?: 0
        StorageRepository.PreviousVault(createdAt = shared.createdAt, itemCount = count)
    }

    override suspend fun adoptPreviousVault(pin: String): Result<Unit> = runSuspendCatching {
        val shared = checkNotNull(configStore.readShared()) { "No previous vault" }
        val keys = checkNotNull(keyring.unwrap(shared, shared.pin, pin)) { "Wrong PIN" }
        // Anything hidden with the temporary vault cannot be read with the old keys; set it aside.
        withContext(dispatchers.io) {
            moveAside(paths.fallbackRoot)
            dao.clear()
        }
        configStore.adopt(shared)
        auth.open(keys)
    }

    override suspend fun archivePreviousVault(): Result<Unit> = runSuspendCatching {
        withContext(dispatchers.io) { moveAside(paths.sharedRoot) }
        configStore.mirror()
    }

    override suspend fun syncConfigToSharedStorage(): Result<Unit> = runSuspendCatching { configStore.mirror() }

    private fun moveAside(root: File) {
        if (!root.exists()) return
        val target = File(root.parentFile, "${root.name}_archived_${System.currentTimeMillis()}")
        check(root.renameTo(target)) { "Could not archive $root" }
    }
}
