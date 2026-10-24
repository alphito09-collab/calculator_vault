package com.calculator.vault.feature.importer.domain.usecase

import com.calculator.vault.core.domain.repository.DeviceMediaRepository
import com.calculator.vault.core.model.DeviceAlbum
import com.calculator.vault.core.model.DeviceMedia
import com.calculator.vault.core.model.MediaType
import org.koin.core.annotation.Factory

@Factory
class LoadDeviceAlbumsUseCase(private val repository: DeviceMediaRepository) {
    suspend operator fun invoke(type: MediaType?): List<DeviceAlbum> = repository.loadAlbums(type)
}

@Factory
class ResolvePickedDocumentsUseCase(private val repository: DeviceMediaRepository) {
    suspend operator fun invoke(uris: List<String>): List<DeviceMedia> = repository.resolveDocuments(uris)
}

/** What the importer may list, and what to ask for when it can't. */
@Factory
class MediaAccessUseCase(private val repository: DeviceMediaRepository) {
    fun hasAccess(type: MediaType?): Boolean = repository.hasMediaAccess(type)
    fun permissions(type: MediaType?): List<String> = repository.mediaPermissions(type)
}
