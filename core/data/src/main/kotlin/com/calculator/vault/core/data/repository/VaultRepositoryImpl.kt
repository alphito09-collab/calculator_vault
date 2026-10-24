package com.calculator.vault.core.data.repository

import android.content.Context
import android.net.Uri
import com.calculator.vault.core.common.VaultConstants
import com.calculator.vault.core.common.dispatchers.VaultDispatchers
import com.calculator.vault.core.common.result.runSuspendCatching
import com.calculator.vault.core.data.media.MediaRestorer
import com.calculator.vault.core.data.media.OriginalsRemover
import com.calculator.vault.core.data.media.ThumbnailFactory
import com.calculator.vault.core.data.storage.ItemMeta
import com.calculator.vault.core.data.storage.VaultFileStore
import com.calculator.vault.core.data.storage.VaultPaths
import com.calculator.vault.core.data.storage.asMeta
import com.calculator.vault.core.data.storage.asModel
import com.calculator.vault.core.database.dao.VaultItemDao
import com.calculator.vault.core.database.entity.asEntity
import com.calculator.vault.core.database.entity.asModel
import com.calculator.vault.core.domain.repository.VaultRepository
import com.calculator.vault.core.model.DeviceMedia
import com.calculator.vault.core.model.MediaType
import com.calculator.vault.core.model.TransferProgress
import com.calculator.vault.core.model.VaultCounts
import com.calculator.vault.core.model.VaultItem
import com.calculator.vault.core.security.session.SessionKeys
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import timber.log.Timber
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.coroutines.coroutineContext

@Single(binds = [VaultRepository::class])
class VaultRepositoryImpl(
    private val context: Context,
    private val dao: VaultItemDao,
    private val store: VaultFileStore,
    private val paths: VaultPaths,
    private val session: SessionKeys,
    private val thumbnails: ThumbnailFactory,
    private val remover: OriginalsRemover,
    private val restorer: MediaRestorer,
    private val dispatchers: VaultDispatchers,
) : VaultRepository {

    override fun observeItems(type: MediaType): Flow<List<VaultItem>> =
        dao.observeByType(type).map { list -> list.map { it.asModel() } }.distinctUntilChanged()

    override fun observeTrash(): Flow<List<VaultItem>> =
        dao.observeTrash().map { list -> list.map { it.asModel() } }

    override fun observeCounts(): Flow<VaultCounts> = dao.observeCounts().map { counts ->
        val byType = counts.associate { it.type to it.count }
        VaultCounts(
            photos = byType[MediaType.PHOTO] ?: 0,
            videos = byType[MediaType.VIDEO] ?: 0,
            audios = byType[MediaType.AUDIO] ?: 0,
            files = byType[MediaType.FILE] ?: 0,
        )
    }.distinctUntilChanged()

    override fun observeItem(id: String): Flow<VaultItem?> = dao.observe(id).map { it?.asModel() }

    override suspend fun getItem(id: String): VaultItem? = dao.get(id)?.asModel()

    override fun hide(sources: List<DeviceMedia>, deleteOriginals: Boolean): Flow<TransferProgress> = flow {
        var done = 0
        var failed = 0
        emit(TransferProgress(0, sources.size))
        session.withKeys { cipher ->
            for (source in sources) {
                coroutineContext.ensureActive()
                emit(TransferProgress(done, sources.size, failed, source.name))
                val ok = runCatching {
                    val id = UUID.randomUUID().toString()
                    val uri = Uri.parse(source.uri)
                    val written = context.contentResolver.openInputStream(uri)?.use { input ->
                        store.writeContent(cipher, id, input)
                    } ?: error("Cannot open ${source.uri}")
                    val thumb = thumbnails.fromUri(uri, source.type, source.mimeType)
                    if (thumb != null) store.writeThumbnail(cipher, id, thumb)
                    val item = VaultItem(
                        id = id,
                        type = source.type,
                        name = source.name,
                        mimeType = source.mimeType,
                        sizeBytes = if (written > 0) written else source.sizeBytes,
                        originalPath = source.path,
                        album = source.album,
                        dateTaken = source.dateTaken,
                        dateAdded = System.currentTimeMillis(),
                        durationMs = source.durationMs,
                        width = source.width,
                        height = source.height,
                        hasThumbnail = thumb != null,
                    )
                    store.writeMeta(cipher, item.asMeta())
                    dao.upsert(item.asEntity())
                    if (deleteOriginals) remover.delete(source.uri, source.path)
                }.onFailure { Timber.e(it, "Hiding ${source.name} failed") }.isSuccess
                if (ok) done++ else failed++
            }
        }
        emit(TransferProgress(done, sources.size, failed))
    }.flowOn(dispatchers.io)

    override suspend fun addCapture(file: File, type: MediaType, mimeType: String): Result<VaultItem> =
        withContext(dispatchers.io) {
            runSuspendCatching {
                session.withKeys { cipher ->
                    val id = UUID.randomUUID().toString()
                    val size = file.inputStream().use { store.writeContent(cipher, id, it) }
                    val thumb = thumbnails.fromFile(file, type, mimeType)
                    if (thumb != null) store.writeThumbnail(cipher, id, thumb)
                    val now = System.currentTimeMillis()
                    val item = VaultItem(
                        id = id,
                        type = type,
                        name = file.name,
                        mimeType = mimeType,
                        sizeBytes = size,
                        originalPath = null,
                        album = CAMERA_ALBUM,
                        dateTaken = now,
                        dateAdded = now,
                        durationMs = if (type == MediaType.VIDEO) mediaDuration(file) else null,
                        hasThumbnail = thumb != null,
                    )
                    store.writeMeta(cipher, item.asMeta())
                    dao.upsert(item.asEntity())
                    item
                }
            }.also { file.delete() }
        }

    override fun unhide(ids: List<String>): Flow<TransferProgress> = flow {
        var done = 0
        var failed = 0
        val items = dao.getAll(ids).map { it.asModel() }
        emit(TransferProgress(0, items.size))
        session.withKeys {
            for (item in items) {
                coroutineContext.ensureActive()
                emit(TransferProgress(done, items.size, failed, item.name))
                val ok = runCatching {
                    store.openContent(item.id).use { restorer.restore(item, it) }
                    store.delete(item.id)
                    dao.delete(listOf(item.id))
                }.onFailure { Timber.e(it, "Unhide ${item.name} failed") }.isSuccess
                if (ok) done++ else failed++
            }
        }
        emit(TransferProgress(done, items.size, failed))
    }.flowOn(dispatchers.io)

    override suspend fun rename(id: String, newName: String): Result<Unit> = updateMeta(listOf(id)) { it.copy(name = newName) }
        .onSuccess { dao.rename(id, newName) }

    override suspend fun moveToTrash(ids: List<String>): Result<Unit> {
        val now = System.currentTimeMillis()
        return updateMeta(ids) { it.copy(trashedAt = now) }.onSuccess { dao.setTrashedAt(ids, now) }
    }

    override suspend fun restoreFromTrash(ids: List<String>): Result<Unit> =
        updateMeta(ids) { it.copy(trashedAt = null) }.onSuccess { dao.setTrashedAt(ids, null) }

    override suspend fun deletePermanently(ids: List<String>): Result<Unit> = withContext(dispatchers.io) {
        runSuspendCatching {
            ids.forEach(store::delete)
            dao.delete(ids)
        }
    }

    override suspend fun emptyTrash(): Result<Unit> = withContext(dispatchers.io) {
        runSuspendCatching { deletePermanently(dao.allTrashed().map { it.id }).getOrThrow() }
    }

    override suspend fun decryptToCache(ids: List<String>): Result<List<File>> = withContext(dispatchers.io) {
        runSuspendCatching {
            session.withKeys {
                dao.getAll(ids).map { entity ->
                    val dir = File(paths.decryptedCacheDir, entity.id).apply { mkdirs() }
                    val target = File(dir, entity.name)
                    if (!target.exists() || target.length() == 0L) {
                        store.openContent(entity.id).use { input -> target.outputStream().use { input.copyTo(it) } }
                    }
                    target
                }
            }
        }
    }

    override suspend fun rebuildIndexIfNeeded(): Result<Int> = withContext(dispatchers.io) {
        runSuspendCatching {
            session.withKeys { cipher ->
                val onDisk = store.listItemIds()
                val indexed = dao.count()
                var restored = 0
                if (indexed < onDisk.size) {
                    val entities = onDisk.mapNotNull { id -> store.readMeta(cipher, id)?.asModel()?.asEntity() }
                    dao.upsertAll(entities)
                    restored = entities.size
                }
                purgeExpiredTrash()
                restored
            }
        }
    }

    private suspend fun purgeExpiredTrash() {
        val cutoff = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(VaultConstants.TRASH_RETENTION_DAYS.toLong())
        val expired = dao.trashedBefore(cutoff).map { it.id }
        if (expired.isNotEmpty()) {
            expired.forEach(store::delete)
            dao.delete(expired)
        }
    }

    private suspend fun updateMeta(ids: List<String>, transform: (ItemMeta) -> ItemMeta): Result<Unit> =
        withContext(dispatchers.io) {
            runSuspendCatching {
                session.withKeys { cipher ->
                    dao.getAll(ids).forEach { entity ->
                        val meta = store.readMeta(cipher, entity.id) ?: entity.asModel().asMeta()
                        store.writeMeta(cipher, transform(meta))
                    }
                }
            }
        }

    private fun mediaDuration(file: File): Long? = runCatching {
        android.media.MediaMetadataRetriever().run {
            try {
                setDataSource(file.absolutePath)
                extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
            } finally {
                release()
            }
        }
    }.getOrNull()

    private companion object {
        const val CAMERA_ALBUM = "Camera"
    }
}
