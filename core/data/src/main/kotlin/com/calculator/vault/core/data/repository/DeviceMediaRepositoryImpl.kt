package com.calculator.vault.core.data.repository

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.core.content.ContextCompat
import com.calculator.vault.core.common.dispatchers.VaultDispatchers
import com.calculator.vault.core.domain.repository.DeviceMediaRepository
import com.calculator.vault.core.model.DeviceAlbum
import com.calculator.vault.core.model.DeviceMedia
import com.calculator.vault.core.model.MediaType
import com.calculator.vault.core.model.SpecialAlbums
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import timber.log.Timber
import java.io.File

@Single(binds = [DeviceMediaRepository::class])
class DeviceMediaRepositoryImpl(
    private val context: Context,
    private val dispatchers: VaultDispatchers,
) : DeviceMediaRepository {

    @Suppress("DEPRECATION")
    private val storageRoot: String = Environment.getExternalStorageDirectory().absolutePath

    override suspend fun loadAlbums(type: MediaType?): List<DeviceAlbum> = withContext(dispatchers.io) {
        val types = type?.let(::listOf) ?: MediaType.entries
        val media = types.flatMap { runCatching { query(it) }.onFailure { e -> Timber.w(e, "Query $it failed") }.getOrDefault(emptyList()) }
        media
            .groupBy { it.album }
            .map { (name, items) -> DeviceAlbum(name, items.sortedByDescending(DeviceMedia::dateTaken)) }
            .sortedWith(
                compareByDescending<DeviceAlbum> { it.name.equals("Camera", ignoreCase = true) }
                    .thenByDescending { it.items.firstOrNull()?.dateTaken ?: 0L },
            )
    }

    override suspend fun resolveDocuments(uris: List<String>): List<DeviceMedia> = withContext(dispatchers.io) {
        uris.mapNotNull { raw ->
            val uri = Uri.parse(raw)
            runCatching {
                val resolver = context.contentResolver
                var name = uri.lastPathSegment ?: "file"
                var size = 0L
                var modified = System.currentTimeMillis()
                resolver.query(uri, null, null, null, null)?.use { c ->
                    if (c.moveToFirst()) {
                        c.string(OpenableColumns.DISPLAY_NAME)?.let { name = it }
                        size = c.long(OpenableColumns.SIZE) ?: 0L
                        c.long(DocumentsContract.Document.COLUMN_LAST_MODIFIED)?.takeIf { it > 0 }?.let { modified = it }
                    }
                }
                val mime = resolver.getType(uri) ?: mimeFromName(name)
                DeviceMedia(
                    uri = raw,
                    type = typeForMime(mime),
                    name = name,
                    mimeType = mime,
                    sizeBytes = size,
                    album = SpecialAlbums.DEVICE_STORAGE,
                    dateTaken = modified,
                )
            }.onFailure { Timber.w(it, "Resolve $raw failed") }.getOrNull()
        }
    }

    override fun mediaPermissions(type: MediaType?): List<String> = when {
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU -> listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        else -> when (type) {
            MediaType.PHOTO -> listOf(Manifest.permission.READ_MEDIA_IMAGES)
            MediaType.VIDEO -> listOf(Manifest.permission.READ_MEDIA_VIDEO)
            MediaType.AUDIO -> listOf(Manifest.permission.READ_MEDIA_AUDIO)
            MediaType.FILE -> emptyList()
            null -> listOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_AUDIO)
        }
    }

    override fun hasMediaAccess(type: MediaType?): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) return true
        if (mediaPermissions(type).any(::granted)) return true
        // Android 14+ "Select photos and videos": only the chosen items are visible, but that is access.
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
            (type == null || type == MediaType.PHOTO || type == MediaType.VIDEO) &&
            granted(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
    }

    private fun granted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    private fun query(type: MediaType): List<DeviceMedia> {
        val collection = collectionFor(type)
        val projection = buildList {
            add(MediaStore.MediaColumns._ID)
            add(MediaStore.MediaColumns.DISPLAY_NAME)
            add(MediaStore.MediaColumns.MIME_TYPE)
            add(MediaStore.MediaColumns.SIZE)
            add(MediaStore.MediaColumns.DATE_MODIFIED)
            add(MediaStore.MediaColumns.WIDTH)
            add(MediaStore.MediaColumns.HEIGHT)
            @Suppress("DEPRECATION") add(MediaStore.MediaColumns.DATA)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                add(MediaStore.MediaColumns.DATE_TAKEN)
                add(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME)
                add(MediaStore.MediaColumns.DURATION)
            }
            if (type == MediaType.PHOTO || type == MediaType.VIDEO) add(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME)
            if (type == MediaType.VIDEO) add(MediaStore.Video.VideoColumns.DURATION)
            if (type == MediaType.AUDIO) add(MediaStore.Audio.AudioColumns.DURATION)
        }.distinct().toTypedArray()

        val (selection, args) = when (type) {
            MediaType.FILE -> {
                val placeholders = DOCUMENT_MIME_TYPES.joinToString(",") { "?" }
                "${MediaStore.MediaColumns.MIME_TYPE} IN ($placeholders) AND ${MediaStore.MediaColumns.SIZE} > 0" to DOCUMENT_MIME_TYPES.toTypedArray()
            }
            else -> "${MediaStore.MediaColumns.SIZE} > 0" to null
        }

        val result = ArrayList<DeviceMedia>()
        context.contentResolver.query(collection, projection, selection, args, "${MediaStore.MediaColumns.DATE_MODIFIED} DESC")?.use { c ->
            while (c.moveToNext()) {
                val id = c.long(MediaStore.MediaColumns._ID) ?: continue
                @Suppress("DEPRECATION")
                val path = c.string(MediaStore.MediaColumns.DATA)
                val name = c.string(MediaStore.MediaColumns.DISPLAY_NAME) ?: path?.substringAfterLast('/') ?: continue
                val mime = c.string(MediaStore.MediaColumns.MIME_TYPE) ?: mimeFromName(name)
                val taken = c.long(MediaStore.MediaColumns.DATE_TAKEN)?.takeIf { it > 0 }
                    ?: ((c.long(MediaStore.MediaColumns.DATE_MODIFIED) ?: 0L) * 1000L)
                val parent = path?.let { File(it).parentFile }
                val bucket = if (parent?.absolutePath == storageRoot) {
                    SpecialAlbums.STORAGE_ROOT
                } else {
                    c.string(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME) ?: parent?.name ?: SpecialAlbums.OTHER
                }
                result += DeviceMedia(
                    uri = ContentUris.withAppendedId(collection, id).toString(),
                    type = type,
                    name = name,
                    mimeType = mime,
                    sizeBytes = c.long(MediaStore.MediaColumns.SIZE) ?: 0L,
                    album = bucket,
                    dateTaken = taken,
                    path = path,
                    durationMs = c.long(MediaStore.MediaColumns.DURATION)?.takeIf { it > 0 },
                    width = c.int(MediaStore.MediaColumns.WIDTH)?.takeIf { it > 0 },
                    height = c.int(MediaStore.MediaColumns.HEIGHT)?.takeIf { it > 0 },
                )
            }
        }
        return result
    }

    private fun collectionFor(type: MediaType): Uri {
        val volume = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaStore.VOLUME_EXTERNAL else "external"
        return when (type) {
            MediaType.PHOTO -> MediaStore.Images.Media.getContentUri(volume)
            MediaType.VIDEO -> MediaStore.Video.Media.getContentUri(volume)
            MediaType.AUDIO -> MediaStore.Audio.Media.getContentUri(volume)
            MediaType.FILE -> MediaStore.Files.getContentUri(volume)
        }
    }

    companion object {

        val DOCUMENT_MIME_TYPES = listOf(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "text/plain",
            "text/csv",
            "application/rtf",
            "application/zip",
            "application/epub+zip",
            "application/vnd.android.package-archive",
        )

        fun typeForMime(mime: String): MediaType = when {
            mime.startsWith("image/") -> MediaType.PHOTO
            mime.startsWith("video/") -> MediaType.VIDEO
            mime.startsWith("audio/") -> MediaType.AUDIO
            else -> MediaType.FILE
        }

        fun mimeFromName(name: String): String =
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(name.substringAfterLast('.', "").lowercase())
                ?: "application/octet-stream"
    }
}

private fun Cursor.index(column: String): Int = getColumnIndex(column)

internal fun Cursor.string(column: String): String? = index(column).takeIf { it >= 0 && !isNull(it) }?.let(::getString)

internal fun Cursor.long(column: String): Long? = index(column).takeIf { it >= 0 && !isNull(it) }?.let(::getLong)

internal fun Cursor.int(column: String): Int? = index(column).takeIf { it >= 0 && !isNull(it) }?.let(::getInt)
