package com.calculator.vault.core.model

/** The four kinds of content the vault can hide. */
enum class MediaType {
    PHOTO,
    VIDEO,
    AUDIO,
    FILE,
}

/** An encrypted item stored inside the vault. */
data class VaultItem(
    val id: String,
    val type: MediaType,
    val name: String,
    val mimeType: String,
    val sizeBytes: Long,
    val originalPath: String?,
    val album: String?,
    val dateTaken: Long,
    val dateAdded: Long,
    val durationMs: Long? = null,
    val width: Int? = null,
    val height: Int? = null,
    val trashedAt: Long? = null,
    val hasThumbnail: Boolean = false,
) {
    val isTrashed: Boolean get() = trashedAt != null

    val extension: String get() = name.substringAfterLast('.', missingDelimiterValue = "").lowercase()
}

/** Number of visible (non-trashed) items per type. */
data class VaultCounts(
    val photos: Int = 0,
    val videos: Int = 0,
    val audios: Int = 0,
    val files: Int = 0,
) {
    operator fun get(type: MediaType): Int = when (type) {
        MediaType.PHOTO -> photos
        MediaType.VIDEO -> videos
        MediaType.AUDIO -> audios
        MediaType.FILE -> files
    }
}
