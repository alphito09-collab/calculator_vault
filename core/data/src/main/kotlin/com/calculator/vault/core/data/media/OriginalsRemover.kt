package com.calculator.vault.core.data.media

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import com.calculator.vault.core.data.storage.StorageAccess
import org.koin.core.annotation.Single
import timber.log.Timber
import java.io.File

/**
 * Deletes originals after they were safely encrypted. With All-files access this needs no
 * per-file confirmation; otherwise MediaStore deletes of other apps' files may be refused,
 * in which case the original simply stays (the encrypted copy is still in the vault).
 */
@Single
class OriginalsRemover(
    private val context: Context,
    private val access: StorageAccess,
) {
    fun delete(uri: String, path: String?): Boolean {
        val parsed = Uri.parse(uri)
        var deleted = false
        if (path != null && access.hasAllFilesAccess()) {
            val file = File(path)
            deleted = file.exists() && file.delete()
            if (deleted) MediaScannerConnection.scanFile(context, arrayOf(path), null, null)
        }
        if (parsed.scheme == "content") {
            val rows = runCatching { context.contentResolver.delete(parsed, null, null) }
                .onFailure { Timber.i(it, "MediaStore delete refused for $uri") }
                .getOrDefault(0)
            deleted = deleted || rows > 0
        }
        return deleted
    }
}
