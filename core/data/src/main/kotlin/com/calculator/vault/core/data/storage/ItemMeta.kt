package com.calculator.vault.core.data.storage

import com.calculator.vault.core.model.MediaType
import com.calculator.vault.core.model.VaultItem
import kotlinx.serialization.Serializable

/** Encrypted sidecar (`.cvm`) describing one item; the index is rebuilt from these. */
@Serializable
internal data class ItemMeta(
    val id: String,
    val type: MediaType,
    val name: String,
    val mimeType: String,
    val sizeBytes: Long,
    val originalPath: String? = null,
    val album: String? = null,
    val dateTaken: Long,
    val dateAdded: Long,
    val durationMs: Long? = null,
    val width: Int? = null,
    val height: Int? = null,
    val trashedAt: Long? = null,
    val hasThumbnail: Boolean = false,
)

internal fun ItemMeta.asModel() = VaultItem(
    id, type, name, mimeType, sizeBytes, originalPath, album, dateTaken, dateAdded, durationMs, width, height, trashedAt, hasThumbnail,
)

internal fun VaultItem.asMeta() = ItemMeta(
    id, type, name, mimeType, sizeBytes, originalPath, album, dateTaken, dateAdded, durationMs, width, height, trashedAt, hasThumbnail,
)
