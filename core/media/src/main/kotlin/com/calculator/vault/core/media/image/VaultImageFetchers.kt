package com.calculator.vault.core.media.image

import android.content.pm.PackageManager
import coil3.ImageLoader
import coil3.asImage
import coil3.decode.DataSource
import coil3.decode.ImageSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.ImageFetchResult
import coil3.fetch.SourceFetchResult
import coil3.key.Keyer
import coil3.request.Options
import com.calculator.vault.core.data.storage.VaultFileStore
import com.calculator.vault.core.model.AppIconRequest
import com.calculator.vault.core.model.IntruderPhotoRequest
import com.calculator.vault.core.model.VaultImageRequest
import com.calculator.vault.core.model.VaultThumbnailRequest
import okio.Buffer
import okio.FileSystem
import okio.buffer
import okio.source

/**
 * Coil fetchers that decrypt vault images straight into memory. Nothing decrypted is ever
 * written to Coil's disk cache (it is disabled for the app's ImageLoader).
 */
class VaultThumbnailFetcher(private val request: VaultThumbnailRequest, private val store: VaultFileStore) : Fetcher {
    override suspend fun fetch(): FetchResult? {
        val bytes = store.readThumbnail(request.itemId) ?: return null
        return SourceFetchResult(ImageSource(Buffer().write(bytes), FileSystem.SYSTEM), "image/jpeg", DataSource.DISK)
    }

    class Factory(private val store: VaultFileStore) : Fetcher.Factory<VaultThumbnailRequest> {
        override fun create(data: VaultThumbnailRequest, options: Options, imageLoader: ImageLoader) = VaultThumbnailFetcher(data, store)
    }
}

class VaultImageFetcher(private val request: VaultImageRequest, private val store: VaultFileStore) : Fetcher {
    override suspend fun fetch(): FetchResult {
        val source = store.openContent(request.itemId).source().buffer()
        return SourceFetchResult(ImageSource(source, FileSystem.SYSTEM), null, DataSource.DISK)
    }

    class Factory(private val store: VaultFileStore) : Fetcher.Factory<VaultImageRequest> {
        override fun create(data: VaultImageRequest, options: Options, imageLoader: ImageLoader) = VaultImageFetcher(data, store)
    }
}

class IntruderPhotoFetcher(private val request: IntruderPhotoRequest, private val store: VaultFileStore) : Fetcher {
    override suspend fun fetch(): FetchResult? {
        val bytes = store.readIntruderPhoto(request.attemptId) ?: return null
        return SourceFetchResult(ImageSource(Buffer().write(bytes), FileSystem.SYSTEM), "image/jpeg", DataSource.DISK)
    }

    class Factory(private val store: VaultFileStore) : Fetcher.Factory<IntruderPhotoRequest> {
        override fun create(data: IntruderPhotoRequest, options: Options, imageLoader: ImageLoader) = IntruderPhotoFetcher(data, store)
    }
}

class AppIconFetcher(private val request: AppIconRequest, private val packageManager: PackageManager) : Fetcher {
    override suspend fun fetch(): FetchResult? {
        val drawable = runCatching { packageManager.getApplicationIcon(request.packageName) }.getOrNull() ?: return null
        return ImageFetchResult(drawable.asImage(), isSampled = false, dataSource = DataSource.DISK)
    }

    class Factory(private val packageManager: PackageManager) : Fetcher.Factory<AppIconRequest> {
        override fun create(data: AppIconRequest, options: Options, imageLoader: ImageLoader) = AppIconFetcher(data, packageManager)
    }
}

internal object VaultKeyers {
    val thumbnail = Keyer<VaultThumbnailRequest> { data, _ -> "vault-thumb:${data.itemId}" }
    val image = Keyer<VaultImageRequest> { data, _ -> "vault-image:${data.itemId}" }
    val intruder = Keyer<IntruderPhotoRequest> { data, _ -> "intruder:${data.attemptId}" }
    val appIcon = Keyer<AppIconRequest> { data, _ -> "app-icon:${data.packageName}" }
}
