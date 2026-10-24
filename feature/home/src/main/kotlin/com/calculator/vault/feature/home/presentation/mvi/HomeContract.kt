package com.calculator.vault.feature.home.presentation.mvi

import com.calculator.vault.core.domain.repository.StorageRepository
import com.calculator.vault.core.model.MediaType
import com.calculator.vault.core.model.VaultCounts
import com.calculator.vault.core.navigation.MainTab
import com.calculator.vault.core.ui.mvi.UiEffect
import com.calculator.vault.core.ui.mvi.UiIntent
import com.calculator.vault.core.ui.mvi.UiState
import com.calculator.vault.feature.home.domain.usecase.HomeBanner

enum class HomeTool { APP_LOCK, THEMES, CLOUD_BACKUP, INTRUDER }

enum class AddAction { CAMERA, MUSIC, FILES, VIDEOS, PHOTOS }

data class HomeState(
    val tab: MainTab = MainTab.HOME,
    val counts: VaultCounts = VaultCounts(),
    val banner: HomeBanner? = null,
    val addMenuOpen: Boolean = false,
    val showPermissionDialog: Boolean = false,
    val previousVault: StorageRepository.PreviousVault? = null,
    val restorePin: String = "",
    val restoreError: Boolean = false,
    val restoring: Boolean = false,
    val showExitDialog: Boolean = false,
) : UiState

sealed interface HomeIntent : UiIntent {
    data class SelectTab(val tab: MainTab) : HomeIntent
    data object Resumed : HomeIntent
    data object ToggleAddMenu : HomeIntent
    data object CloseAddMenu : HomeIntent
    data class Add(val action: AddAction) : HomeIntent
    data class OpenCategory(val type: MediaType) : HomeIntent
    data class OpenTool(val tool: HomeTool) : HomeIntent
    data object BannerClicked : HomeIntent
    data object BannerDismissed : HomeIntent
    data object AllowFilesAccess : HomeIntent
    data object DenyFilesAccess : HomeIntent
    data class RestorePinChanged(val pin: String) : HomeIntent
    data object RestorePreviousVault : HomeIntent
    data object StartFresh : HomeIntent
    data object BackPressed : HomeIntent
    data object DismissExit : HomeIntent
    data object ConfirmExit : HomeIntent
}

sealed interface HomeEffect : UiEffect {
    data class OpenCategory(val type: MediaType) : HomeEffect
    data class Import(val type: MediaType) : HomeEffect
    data object OpenCamera : HomeEffect
    data class OpenTool(val tool: HomeTool) : HomeEffect
    data object OpenRecovery : HomeEffect
    data object OpenCloudBackup : HomeEffect
    data object OpenFilesAccessSettings : HomeEffect
    data object VaultRestored : HomeEffect
    data object ExitApp : HomeEffect
}
