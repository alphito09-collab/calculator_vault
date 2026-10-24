package com.calculator.vault.core.data.storage

import com.calculator.vault.core.security.crypto.VaultCipher
import com.calculator.vault.core.security.session.SessionKeys
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single
import java.io.File
import java.io.InputStream
import java.io.RandomAccessFile
import java.nio.channels.SeekableByteChannel

/**
 * Encrypted on-disk representation of vault items:
 * - `items/<id>.cvx` streaming-AEAD content (associated data = id)
 * - `items/<id>.cvm` AEAD metadata JSON
 * - `thumbs/<id>.cvt` AEAD thumbnail JPEG
 */
@Single
class VaultFileStore(
    private val paths: VaultPaths,
    private val session: SessionKeys,
) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    /** Encrypts [source] into the content file and returns the plaintext byte count. */
    fun writeContent(cipher: VaultCipher, id: String, source: InputStream): Long {
        paths.ensureDirs()
        var total = 0L
        paths.content(id).writeAtomically { out ->
            cipher.encryptingStream(out, contentAd(id)).use { enc ->
                val buffer = ByteArray(BUFFER)
                while (true) {
                    val read = source.read(buffer)
                    if (read == -1) break
                    enc.write(buffer, 0, read)
                    total += read
                }
            }
        }
        return total
    }

    fun openContent(id: String): InputStream =
        session.require().decryptingStream(paths.locateContent(id).inputStream().buffered(BUFFER), contentAd(id))

    /** Seekable plaintext view for media players; the caller closes it. */
    fun openContentChannel(id: String): SeekableByteChannel {
        val raw = RandomAccessFile(paths.locateContent(id), "r").channel
        return session.require().decryptingChannel(raw, contentAd(id))
    }

    internal fun writeMeta(cipher: VaultCipher, meta: ItemMeta) {
        val sealed = cipher.seal(json.encodeToString(ItemMeta.serializer(), meta).toByteArray(), metaAd(meta.id))
        paths.meta(meta.id).writeAtomically { it.write(sealed) }
    }

    internal fun readMeta(cipher: VaultCipher, id: String): ItemMeta? = runCatching {
        val file = paths.locateMeta(id)
        if (!file.exists()) return null
        json.decodeFromString(ItemMeta.serializer(), cipher.open(file.readBytes(), metaAd(id)).decodeToString())
    }.getOrNull()

    fun writeThumbnail(cipher: VaultCipher, id: String, jpeg: ByteArray) {
        paths.ensureDirs()
        paths.thumb(id).writeAtomically { it.write(cipher.seal(jpeg, thumbAd(id))) }
    }

    fun readThumbnail(id: String): ByteArray? {
        val cipher = session.cipherOrNull() ?: return null
        val file = paths.locateThumb(id)
        if (!file.exists()) return null
        return runCatching { cipher.open(file.readBytes(), thumbAd(id)) }.getOrNull()
    }

    fun readIntruderPhoto(id: String): ByteArray? {
        val cipher = session.cipherOrNull() ?: return null
        val file = paths.locateIntruderPhoto(id)
        if (!file.exists()) return null
        return runCatching { cipher.openSealedForVault(file.readBytes(), intruderContext(id)) }.getOrNull()
    }

    fun delete(id: String) {
        listOf(paths.sharedRoot, paths.fallbackRoot).forEach { base ->
            paths.content(id, base).delete()
            paths.meta(id, base).delete()
            paths.thumb(id, base).delete()
        }
    }

    /** Ids of every item that has a metadata sidecar, across both roots. */
    fun listItemIds(): Set<String> = listOf(paths.sharedRoot, paths.fallbackRoot)
        .flatMap { base -> File(base, "items").listFiles { f -> f.name.endsWith(VaultPaths.META_EXT) }.orEmpty().toList() }
        .map { it.name.removeSuffix(VaultPaths.META_EXT) }
        .toSet()

    /** Moves everything from the app-specific fallback into the shared folder. */
    fun migrateFallbackToShared(): Int {
        val from = paths.fallbackRoot
        if (!from.exists() || from == paths.root) return 0
        paths.ensureDirs(paths.sharedRoot)
        var moved = 0
        listOf("items", "thumbs", "intruders").forEach { dir ->
            File(from, dir).listFiles().orEmpty().forEach { file ->
                val target = File(File(paths.sharedRoot, dir), file.name)
                if (file.renameTo(target) || (file.copyTo(target, overwrite = true).exists() && file.delete())) moved++
            }
        }
        return moved
    }

    companion object {
        private const val BUFFER = 256 * 1024
        fun contentAd(id: String) = "cv-content:$id".toByteArray()
        fun metaAd(id: String) = "cv-meta:$id".toByteArray()
        fun thumbAd(id: String) = "cv-thumb:$id".toByteArray()
        fun intruderContext(id: String) = "cv-intruder:$id".toByteArray()
    }
}
