package com.calculator.vault.core.model

/** A file on the device (MediaStore or storage access framework) that can be hidden. */
data class DeviceMedia(
    val uri: String,
    val type: MediaType,
    val name: String,
    val mimeType: String,
    val sizeBytes: Long,
    val album: String,
    val dateTaken: Long,
    val path: String? = null,
    val durationMs: Long? = null,
    val width: Int? = null,
    val height: Int? = null,
)

/** Album keys that are not folder names; the UI shows a localized label for them instead. */
object SpecialAlbums {
    /** Files directly in the root of shared storage (MediaStore reports the bucket as "0"). */
    const val STORAGE_ROOT = "@storage_root"

    /** Files picked through the system document picker. */
    const val DEVICE_STORAGE = "@device_storage"

    const val OTHER = "@other"
}

/** Device media grouped by album/bucket, newest first. */
data class DeviceAlbum(
    val name: String,
    val items: List<DeviceMedia>,
)

/** Progress of a hide/unhide/import batch. */
data class TransferProgress(
    val completed: Int,
    val total: Int,
    val failed: Int = 0,
    val currentName: String? = null,
) {
    val fraction: Float get() = if (total == 0) 1f else completed.toFloat() / total
    val isFinished: Boolean get() = completed + failed >= total
}
