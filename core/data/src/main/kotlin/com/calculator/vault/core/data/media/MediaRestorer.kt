package com.calculator.vault.core.data.media

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.calculator.vault.core.data.storage.StorageAccess
import com.calculator.vault.core.model.MediaType
import com.calculator.vault.core.model.VaultItem
import org.koin.core.annotation.Single
import java.io.File
import java.io.InputStream
import java.io.OutputStream

/** Writes decrypted content back to public storage ("Unhide"). */
@Single
class MediaRestorer(
    private val context: Context,
    private val access: StorageAccess,
) {
    /** Returns the restored location (path or content uri). */
    fun restore(item: VaultItem, source: InputStream): String {
        val original = item.originalPath?.let(::File)
        if (access.hasAllFilesAccess() && original?.parentFile?.let { it.exists() || it.mkdirs() } == true) {
            val target = uniqueFile(original)
            target.outputStream().use { source.copyTo(it, BUFFER) }
            MediaScannerConnection.scanFile(context, arrayOf(target.absolutePath), arrayOf(item.mimeType), null)
            return target.absolutePath
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return insertWithMediaStore(item) { out -> source.copyTo(out, BUFFER) }
        }
        @Suppress("DEPRECATION")
        val dir = File(Environment.getExternalStoragePublicDirectory(publicDirFor(item.type)), RESTORE_FOLDER).apply { mkdirs() }
        val target = uniqueFile(File(dir, item.name))
        target.outputStream().use { source.copyTo(it, BUFFER) }
        MediaScannerConnection.scanFile(context, arrayOf(target.absolutePath), arrayOf(item.mimeType), null)
        return target.absolutePath
    }

    private fun insertWithMediaStore(item: VaultItem, write: (OutputStream) -> Unit): String {
        val resolver = context.contentResolver
        val collection = when (item.type) {
            MediaType.PHOTO -> MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            MediaType.VIDEO -> MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            MediaType.AUDIO -> MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            MediaType.FILE -> MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        }
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, item.name)
            put(MediaStore.MediaColumns.MIME_TYPE, item.mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, "${publicDirFor(item.type)}/$RESTORE_FOLDER")
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val uri = resolver.insert(collection, values) ?: error("MediaStore insert failed")
        try {
            resolver.openOutputStream(uri)?.use(write) ?: error("No output stream for $uri")
            resolver.update(uri, ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }, null, null)
        } catch (error: Throwable) {
            resolver.delete(uri, null, null)
            throw error
        }
        return uri.toString()
    }

    private fun publicDirFor(type: MediaType): String = when (type) {
        MediaType.PHOTO -> Environment.DIRECTORY_PICTURES
        MediaType.VIDEO -> Environment.DIRECTORY_MOVIES
        MediaType.AUDIO -> Environment.DIRECTORY_MUSIC
        MediaType.FILE -> Environment.DIRECTORY_DOWNLOADS
    }

    private fun uniqueFile(desired: File): File {
        if (!desired.exists()) return desired
        val base = desired.nameWithoutExtension
        val ext = desired.extension.let { if (it.isEmpty()) "" else ".$it" }
        var index = 1
        while (true) {
            val candidate = File(desired.parentFile, "$base ($index)$ext")
            if (!candidate.exists()) return candidate
            index++
        }
    }

    private companion object {
        const val RESTORE_FOLDER = "Restored"
        const val BUFFER = 256 * 1024
    }
}
