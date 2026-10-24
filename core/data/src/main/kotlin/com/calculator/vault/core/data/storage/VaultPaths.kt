package com.calculator.vault.core.data.storage

import android.content.Context
import android.os.Environment
import com.calculator.vault.core.common.VaultConstants
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single
import timber.log.Timber
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * File layout of the vault.
 *
 * The primary root is a hidden folder in shared storage so encrypted data survives uninstall.
 * Until "All files access" is granted (or while that folder still belongs to a previous vault),
 * content goes to an app-specific fallback folder and is migrated to the shared root later.
 */
@Single
class VaultPaths(
    private val context: Context,
    private val access: StorageAccess,
) {
    // Debug and release builds can be installed side by side; they must never share one vault.
    private val sharedDirName =
        if (context.packageName.endsWith(DEBUG_PACKAGE_SUFFIX)) "${VaultConstants.VAULT_DIR_NAME}_debug" else VaultConstants.VAULT_DIR_NAME

    val sharedRoot: File get() = File(Environment.getExternalStorageDirectory(), sharedDirName)

    val fallbackRoot: File get() = File(context.getExternalFilesDir(null) ?: context.filesDir, "vault")

    /** Where new content is written right now. */
    val root: File get() = if (access.hasAllFilesAccess() && !sharedRootClaimedByOtherVault()) sharedRoot else fallbackRoot

    private val json = Json { ignoreUnknownKeys = true }
    private val vaultIdCache = ConcurrentHashMap<String, Pair<Long, String>>()

    /**
     * Shared storage can still hold a vault from before a reinstall (a different vaultId). Until
     * the user restores it or starts fresh (which archives it), the current vault must not write
     * content, mirror its config or migrate files there: that would destroy the old vault's keys.
     * An unreadable shared config also counts, so we never overwrite what we can't identify.
     */
    fun sharedRootClaimedByOtherVault(): Boolean {
        if (!access.hasAllFilesAccess()) return false
        val shared = vaultIdOf(sharedConfig()) ?: return false
        return shared != vaultIdOf(privateConfig)
    }

    /** null when the file doesn't exist, "" when it exists but can't be read. */
    private fun vaultIdOf(file: File): String? {
        val stamp = file.lastModified()
        if (stamp == 0L) return null
        vaultIdCache[file.path]?.let { (at, id) -> if (at == stamp) return id }
        val id = runCatching { json.decodeFromString(VaultIdOnly.serializer(), file.readText()).vaultId }
            .onFailure { Timber.w(it, "Unreadable vault config at $file") }
            .getOrDefault("")
        vaultIdCache[file.path] = stamp to id
        return id
    }

    @Serializable
    private data class VaultIdOnly(val vaultId: String)

    val itemsDir: File get() = File(root, "items")
    val thumbsDir: File get() = File(root, "thumbs")
    val intrudersDir: File get() = File(root, "intruders")

    /** Plaintext copies for sharing/PDF rendering. Wiped on every lock. */
    val decryptedCacheDir: File get() = File(context.cacheDir, "decrypted")

    /** Capture/import scratch space; also wiped on lock. */
    val scratchDir: File get() = File(context.cacheDir, "scratch")

    val privateConfig: File get() = File(context.filesDir, CONFIG_FILE)
    fun sharedConfig(root: File = sharedRoot): File = File(root, CONFIG_FILE)

    fun content(id: String, base: File = root) = File(File(base, "items"), "$id$CONTENT_EXT")
    fun meta(id: String, base: File = root) = File(File(base, "items"), "$id$META_EXT")
    fun thumb(id: String, base: File = root) = File(File(base, "thumbs"), "$id$THUMB_EXT")
    fun intruderPhoto(id: String, base: File = root) = File(File(base, "intruders"), "$id$INTRUDER_EXT")

    /** Locates an item's files in either root (content may predate a migration). */
    fun locateContent(id: String): File = listOf(sharedRoot, fallbackRoot)
        .map { content(id, it) }
        .firstOrNull { it.exists() } ?: content(id)

    fun locateMeta(id: String): File = listOf(sharedRoot, fallbackRoot)
        .map { meta(id, it) }
        .firstOrNull { it.exists() } ?: meta(id)

    fun locateThumb(id: String): File = listOf(sharedRoot, fallbackRoot)
        .map { thumb(id, it) }
        .firstOrNull { it.exists() } ?: thumb(id)

    fun locateIntruderPhoto(id: String): File = listOf(sharedRoot, fallbackRoot)
        .map { intruderPhoto(id, it) }
        .firstOrNull { it.exists() } ?: intruderPhoto(id)

    fun ensureDirs(base: File = root) {
        listOf(File(base, "items"), File(base, "thumbs"), File(base, "intruders")).forEach { it.mkdirs() }
        // Keep gallery apps and the media scanner out.
        runCatching { File(base, ".nomedia").createNewFile() }
            .onFailure { Timber.w(it, "Could not create .nomedia") }
    }

    fun wipeDecryptedCache() {
        decryptedCacheDir.deleteRecursively()
        scratchDir.deleteRecursively()
    }

    companion object {
        private const val DEBUG_PACKAGE_SUFFIX = ".debug"
        const val CONFIG_FILE = "vault.json"
        const val CONTENT_EXT = ".cvx"
        const val META_EXT = ".cvm"
        const val THUMB_EXT = ".cvt"
        const val INTRUDER_EXT = ".cvi"
    }
}
