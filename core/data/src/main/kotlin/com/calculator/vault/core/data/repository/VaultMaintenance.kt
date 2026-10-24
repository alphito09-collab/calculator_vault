package com.calculator.vault.core.data.repository

import com.calculator.vault.core.data.config.VaultConfigStore
import com.calculator.vault.core.data.storage.StorageAccess
import com.calculator.vault.core.data.storage.VaultFileStore
import com.calculator.vault.core.domain.repository.VaultRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

/**
 * Housekeeping after every unlock: move fallback content into the shared folder once access
 * exists, mirror the config, rebuild the index after a reinstall and purge expired trash.
 */
@Single
class VaultMaintenance(
    private val access: StorageAccess,
    private val store: VaultFileStore,
    private val configStore: VaultConfigStore,
) : KoinComponent {
    // Resolved lazily to avoid a construction cycle with the repositories that call us.
    private val vault: VaultRepository by inject()
    private val mutex = Mutex()

    suspend fun runAfterUnlock() = mutex.withLock {
        runCatching {
            // Both steps are no-ops while shared storage still holds a previous vault that the user
            // hasn't restored or archived (see VaultPaths.sharedRootClaimedByOtherVault).
            if (access.hasAllFilesAccess()) {
                val moved = store.migrateFallbackToShared()
                if (moved > 0) Timber.i("Migrated $moved files to shared storage")
                configStore.mirror()
            }
            vault.rebuildIndexIfNeeded().onSuccess { restored ->
                if (restored > 0) Timber.i("Rebuilt index with $restored items")
            }
        }.onFailure { Timber.e(it, "Post-unlock maintenance failed") }
    }
}
