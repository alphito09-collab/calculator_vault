package com.calculator.vault.core.media.image

import android.content.Context
import coil3.ImageLoader
import coil3.memory.MemoryCache
import coil3.request.CachePolicy
import coil3.request.crossfade
import coil3.video.VideoFrameDecoder
import com.calculator.vault.core.data.storage.VaultFileStore
import org.koin.core.annotation.Single

/** The app-wide Coil loader: decrypting fetchers, no disk cache, bounded memory cache. */
@Single
class VaultImageLoader(context: Context, store: VaultFileStore) {

    val loader: ImageLoader = ImageLoader.Builder(context)
        .components {
            add(VaultThumbnailFetcher.Factory(store))
            add(VaultImageFetcher.Factory(store))
            add(IntruderPhotoFetcher.Factory(store))
            add(AppIconFetcher.Factory(context.packageManager))
            add(VaultKeyers.thumbnail)
            add(VaultKeyers.image)
            add(VaultKeyers.intruder)
            add(VaultKeyers.appIcon)
            add(VideoFrameDecoder.Factory())
        }
        .memoryCache {
            MemoryCache.Builder()
                .maxSizePercent(context, 0.20)
                .build()
        }
        .diskCachePolicy(CachePolicy.DISABLED)
        .crossfade(180)
        .build()

    /** Drops decrypted bitmaps from memory when the vault locks. */
    fun clearMemory() {
        loader.memoryCache?.clear()
    }
}
