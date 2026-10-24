package com.calculator.vault.core.data.config

import com.calculator.vault.core.common.dispatchers.VaultDispatchers
import com.calculator.vault.core.data.storage.StorageAccess
import com.calculator.vault.core.data.storage.VaultPaths
import com.calculator.vault.core.data.storage.writeAtomically
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single
import timber.log.Timber
import java.io.File

/**
 * Persists [VaultConfig] privately (included in Auto Backup) and mirrors it into the shared
 * vault folder so a reinstall can find it.
 */
@Single
class VaultConfigStore(
    private val paths: VaultPaths,
    private val access: StorageAccess,
    private val dispatchers: VaultDispatchers,
) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val mutex = Mutex()
    private val _config = MutableStateFlow(readFile(paths.privateConfig))
    val config: StateFlow<VaultConfig?> = _config.asStateFlow()

    fun current(): VaultConfig? = _config.value

    suspend fun save(config: VaultConfig) = mutex.withLock {
        withContext(dispatchers.io) {
            val updated = config.copy(updatedAt = System.currentTimeMillis())
            val bytes = json.encodeToString(VaultConfig.serializer(), updated).toByteArray()
            paths.privateConfig.writeAtomically { it.write(bytes) }
            _config.value = updated
            mirrorToShared(bytes)
        }
    }

    /** Re-copies the private config into shared storage (e.g. right after access is granted). */
    suspend fun mirror() = mutex.withLock {
        withContext(dispatchers.io) {
            val current = _config.value ?: return@withContext
            mirrorToShared(json.encodeToString(VaultConfig.serializer(), current).toByteArray())
        }
    }

    fun readShared(): VaultConfig? = readFile(paths.sharedConfig())

    /** Replaces the local config with one found in shared storage (reinstall restore). */
    suspend fun adopt(config: VaultConfig) = save(config)

    private fun mirrorToShared(bytes: ByteArray) {
        // Never replace another vault's config: its wrapped keys are the only way to open it.
        if (!access.hasAllFilesAccess() || paths.sharedRootClaimedByOtherVault()) return
        runCatching {
            paths.ensureDirs(paths.sharedRoot)
            paths.sharedConfig().writeAtomically { it.write(bytes) }
        }.onFailure { Timber.w(it, "Mirroring vault config failed") }
    }

    private fun readFile(file: File): VaultConfig? = runCatching {
        if (!file.exists()) null else json.decodeFromString(VaultConfig.serializer(), file.readText())
    }.onFailure { Timber.e(it, "Corrupt vault config at $file") }.getOrNull()
}
