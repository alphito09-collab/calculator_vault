package com.calculator.vault.core.model

/** Coil request models. Fetchers that decrypt them are registered by the app's ImageLoader. */
data class VaultThumbnailRequest(val itemId: String)

data class VaultImageRequest(val itemId: String)

data class IntruderPhotoRequest(val attemptId: String)

data class AppIconRequest(val packageName: String)
