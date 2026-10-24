package com.calculator.vault.feature.files.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.calculator.vault.core.model.MediaType
import com.calculator.vault.core.navigation.FileViewerRoute
import com.calculator.vault.core.navigation.FilesRoute
import com.calculator.vault.core.navigation.ImporterRoute
import com.calculator.vault.core.navigation.Navigator
import com.calculator.vault.feature.files.presentation.ui.FileViewerRoute as FileViewerScreenRoute
import com.calculator.vault.feature.files.presentation.ui.FilesRoute as FilesScreenRoute

fun EntryProviderScope<NavKey>.filesEntries(navigator: Navigator) {
    entry<FilesRoute> {
        FilesScreenRoute(
            onOpen = { navigator.navigate(FileViewerRoute(it.id)) },
            onImport = { navigator.navigate(ImporterRoute(MediaType.FILE)) },
            onBack = { navigator.back() },
        )
    }
    entry<FileViewerRoute> { route -> FileViewerScreenRoute(route.itemId, onClose = { navigator.back() }) }
}
