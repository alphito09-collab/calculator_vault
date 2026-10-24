package com.calculator.vault.core.domain.repository

import com.calculator.vault.core.model.DeviceAlbum
import com.calculator.vault.core.model.DeviceMedia
import com.calculator.vault.core.model.MediaType

/** Reads shareable media/files on the device (MediaStore + storage access framework). */
interface DeviceMediaRepository {
    /** Albums for [type], or every hideable type when null. */
    suspend fun loadAlbums(type: MediaType?): List<DeviceAlbum>

    /** Resolves documents picked through the system file picker. */
    suspend fun resolveDocuments(uris: List<String>): List<DeviceMedia>

    /**
     * Runtime permissions that let [loadAlbums] list [type] (every type when null). Empty for
     * documents, which only "All files access" exposes.
     */
    fun mediaPermissions(type: MediaType?): List<String>

    /** Whether [loadAlbums] can see the device's [type] media now. An empty result then just means there is none. */
    fun hasMediaAccess(type: MediaType?): Boolean
}
