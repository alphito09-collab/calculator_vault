package com.calculator.vault.feature.audios.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.calculator.vault.core.model.MediaType
import com.calculator.vault.core.navigation.AudioPlayerRoute
import com.calculator.vault.core.navigation.AudiosRoute
import com.calculator.vault.core.navigation.EqualizerRoute
import com.calculator.vault.core.navigation.ImporterRoute
import com.calculator.vault.core.navigation.Navigator
import com.calculator.vault.feature.audios.presentation.ui.AudioPlayerRoute as AudioPlayerScreenRoute
import com.calculator.vault.feature.audios.presentation.ui.AudiosRoute as AudiosScreenRoute
import com.calculator.vault.feature.audios.presentation.ui.EqualizerRoute as EqualizerScreenRoute

fun EntryProviderScope<NavKey>.audiosEntries(navigator: Navigator) {
    entry<AudiosRoute> {
        AudiosScreenRoute(
            onOpenPlayer = { navigator.navigate(AudioPlayerRoute(it)) },
            onImport = { navigator.navigate(ImporterRoute(MediaType.AUDIO)) },
            onBack = { navigator.back() },
        )
    }
    entry<AudioPlayerRoute> { route ->
        AudioPlayerScreenRoute(route.itemId, onOpenEqualizer = { navigator.navigate(EqualizerRoute) }, onClose = { navigator.back() })
    }
    entry<EqualizerRoute> { EqualizerScreenRoute(onClose = { navigator.back() }) }
}
