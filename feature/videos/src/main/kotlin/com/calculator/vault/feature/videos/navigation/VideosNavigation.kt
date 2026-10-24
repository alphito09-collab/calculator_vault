package com.calculator.vault.feature.videos.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.calculator.vault.core.model.MediaType
import com.calculator.vault.core.navigation.ImporterRoute
import com.calculator.vault.core.navigation.Navigator
import com.calculator.vault.core.navigation.VideoPlayerRoute
import com.calculator.vault.core.navigation.VideosRoute
import com.calculator.vault.feature.videos.presentation.ui.VideoPlayerRoute as VideoPlayerScreenRoute
import com.calculator.vault.feature.videos.presentation.ui.VideosRoute as VideosScreenRoute

fun EntryProviderScope<NavKey>.videosEntries(navigator: Navigator) {
    entry<VideosRoute> {
        VideosScreenRoute(
            onOpen = { navigator.navigate(VideoPlayerRoute(it.id)) },
            onImport = { navigator.navigate(ImporterRoute(MediaType.VIDEO)) },
            onBack = { navigator.back() },
        )
    }
    entry<VideoPlayerRoute> { route -> VideoPlayerScreenRoute(itemId = route.itemId, onClose = { navigator.back() }) }
}
