package com.calculator.vault.feature.photos.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.calculator.vault.core.model.MediaType
import com.calculator.vault.core.navigation.ImporterRoute
import com.calculator.vault.core.navigation.Navigator
import com.calculator.vault.core.navigation.PhotoViewerRoute
import com.calculator.vault.core.navigation.PhotosRoute
import com.calculator.vault.feature.photos.presentation.ui.PhotoViewerRoute as PhotoViewerScreenRoute
import com.calculator.vault.feature.photos.presentation.ui.PhotosRoute as PhotosScreenRoute

fun EntryProviderScope<NavKey>.photosEntries(navigator: Navigator) {
    entry<PhotosRoute> {
        PhotosScreenRoute(
            onOpen = { item, slideshow -> navigator.navigate(PhotoViewerRoute(item.id, slideshow)) },
            onImport = { navigator.navigate(ImporterRoute(MediaType.PHOTO)) },
            onBack = { navigator.back() },
        )
    }
    entry<PhotoViewerRoute> { route ->
        PhotoViewerScreenRoute(itemId = route.itemId, slideshow = route.slideshow, onClose = { navigator.back() })
    }
}
