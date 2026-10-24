package com.calculator.vault.feature.home.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.calculator.vault.core.model.MediaType
import com.calculator.vault.core.navigation.AppLockRoute
import com.calculator.vault.core.navigation.AudiosRoute
import com.calculator.vault.core.navigation.CameraRoute
import com.calculator.vault.core.navigation.CloudBackupRoute
import com.calculator.vault.core.navigation.FilesRoute
import com.calculator.vault.core.navigation.ImporterRoute
import com.calculator.vault.core.navigation.IntruderRoute
import com.calculator.vault.core.navigation.MainRoute
import com.calculator.vault.core.navigation.Navigator
import com.calculator.vault.core.navigation.PhotosRoute
import com.calculator.vault.core.navigation.RecoveryMethodRoute
import com.calculator.vault.core.navigation.ThemesRoute
import com.calculator.vault.core.navigation.VideosRoute
import com.calculator.vault.feature.home.presentation.mvi.HomeTool
import com.calculator.vault.feature.home.presentation.ui.MainCallbacks
import com.calculator.vault.feature.home.presentation.ui.MainRoute as MainScreenRoute

fun EntryProviderScope<NavKey>.homeEntries(
    navigator: Navigator,
    settingsContent: @Composable (PaddingValues) -> Unit,
) {
    entry<MainRoute> { route ->
        MainScreenRoute(
            initialTab = route.tab,
            settingsContent = settingsContent,
            callbacks = MainCallbacks(
                onOpenCategory = { type ->
                    navigator.navigate(
                        when (type) {
                            MediaType.PHOTO -> PhotosRoute
                            MediaType.VIDEO -> VideosRoute
                            MediaType.AUDIO -> AudiosRoute
                            MediaType.FILE -> FilesRoute
                        },
                    )
                },
                onImport = { type -> navigator.navigate(ImporterRoute(type)) },
                onOpenCamera = { navigator.navigate(CameraRoute()) },
                onOpenTool = { tool ->
                    navigator.navigate(
                        when (tool) {
                            HomeTool.APP_LOCK -> AppLockRoute
                            HomeTool.THEMES -> ThemesRoute
                            HomeTool.CLOUD_BACKUP -> CloudBackupRoute
                            HomeTool.INTRUDER -> IntruderRoute
                        },
                    )
                },
                onOpenRecovery = { navigator.navigate(RecoveryMethodRoute(fromSettings = true)) },
                onOpenCloudBackup = { navigator.navigate(CloudBackupRoute) },
            ),
        )
    }
}
