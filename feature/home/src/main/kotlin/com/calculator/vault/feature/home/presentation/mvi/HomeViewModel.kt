package com.calculator.vault.feature.home.presentation.mvi

import androidx.lifecycle.viewModelScope
import com.calculator.vault.core.domain.usecase.PinRules
import com.calculator.vault.core.model.MediaType
import com.calculator.vault.core.navigation.MainTab
import com.calculator.vault.core.ui.mvi.MviViewModel
import com.calculator.vault.feature.home.domain.usecase.DismissBannerUseCase
import com.calculator.vault.feature.home.domain.usecase.HomeBanner
import com.calculator.vault.feature.home.domain.usecase.ObserveHomeSummaryUseCase
import com.calculator.vault.feature.home.domain.usecase.VaultStorageUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam

@KoinViewModel
class HomeViewModel(
    @InjectedParam initialTab: MainTab,
    observeSummary: ObserveHomeSummaryUseCase,
    private val dismissBanner: DismissBannerUseCase,
    private val storage: VaultStorageUseCase,
) : MviViewModel<HomeState, HomeIntent, HomeEffect>(HomeState(tab = initialTab)) {

    /** Ask for All-files access at most once per screen visit unless the user tries to add. */
    private var permissionAsked = false

    /** An import was requested before access existed; resume it once access is granted. */
    private var pendingImport: (() -> Unit)? = null

    init {
        observeSummary()
            .onEach { summary -> setState { copy(counts = summary.counts, banner = summary.banner) } }
            .launchIn(viewModelScope)
    }

    override fun handleIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.SelectTab -> setState { copy(tab = intent.tab, addMenuOpen = false) }
            HomeIntent.Resumed -> onResumed()
            HomeIntent.ToggleAddMenu -> setState { copy(addMenuOpen = !addMenuOpen) }
            HomeIntent.CloseAddMenu -> setState { copy(addMenuOpen = false) }
            is HomeIntent.Add -> {
                setState { copy(addMenuOpen = false) }
                requireAccess {
                    sendEffect(
                        when (intent.action) {
                            AddAction.CAMERA -> HomeEffect.OpenCamera
                            AddAction.MUSIC -> HomeEffect.Import(MediaType.AUDIO)
                            AddAction.FILES -> HomeEffect.Import(MediaType.FILE)
                            AddAction.VIDEOS -> HomeEffect.Import(MediaType.VIDEO)
                            AddAction.PHOTOS -> HomeEffect.Import(MediaType.PHOTO)
                        },
                    )
                }
            }
            is HomeIntent.OpenCategory -> sendEffect(HomeEffect.OpenCategory(intent.type))
            is HomeIntent.OpenTool -> sendEffect(HomeEffect.OpenTool(intent.tool))
            HomeIntent.BannerClicked -> when (currentState.banner) {
                HomeBanner.ADD_RECOVERY -> sendEffect(HomeEffect.OpenRecovery)
                HomeBanner.ENABLE_BACKUP -> sendEffect(HomeEffect.OpenCloudBackup)
                HomeBanner.RESET_CODE, null -> Unit
            }
            HomeIntent.BannerDismissed -> currentState.banner?.let { banner -> viewModelScope.launch { dismissBanner(banner) } }
            HomeIntent.AllowFilesAccess -> {
                setState { copy(showPermissionDialog = false) }
                sendEffect(HomeEffect.OpenFilesAccessSettings)
            }
            HomeIntent.DenyFilesAccess -> {
                pendingImport = null
                setState { copy(showPermissionDialog = false) }
            }
            is HomeIntent.RestorePinChanged -> setState {
                copy(restorePin = intent.pin.filter(Char::isDigit).take(PinRules.LENGTH), restoreError = false)
            }
            HomeIntent.RestorePreviousVault -> restore()
            HomeIntent.StartFresh -> viewModelScope.launch {
                storage.archive()
                setState { copy(previousVault = null) }
            }
            HomeIntent.BackPressed -> when {
                currentState.addMenuOpen -> setState { copy(addMenuOpen = false) }
                currentState.tab == MainTab.SETTINGS -> setState { copy(tab = MainTab.HOME) }
                else -> setState { copy(showExitDialog = true) }
            }
            HomeIntent.DismissExit -> setState { copy(showExitDialog = false) }
            HomeIntent.ConfirmExit -> sendEffect(HomeEffect.ExitApp)
        }
    }

    private fun onResumed() {
        if (!storage.hasAccess()) {
            if (!permissionAsked) {
                permissionAsked = true
                setState { copy(showPermissionDialog = true) }
            }
            return
        }
        viewModelScope.launch {
            // Look for a vault left from before a reinstall first; ours may only be mirrored into
            // shared storage once the user has restored or archived it.
            val previous = storage.previousVault()
            setState { copy(previousVault = previous) }
            if (previous == null) {
                storage.syncConfig()
                pendingImport?.invoke()
            }
            pendingImport = null
        }
    }

    private fun requireAccess(action: () -> Unit) {
        if (storage.hasAccess()) action() else {
            pendingImport = action
            setState { copy(showPermissionDialog = true) }
        }
    }

    private fun restore() {
        if (!PinRules.isValid(currentState.restorePin) || currentState.restoring) return
        setState { copy(restoring = true) }
        viewModelScope.launch {
            storage.adopt(currentState.restorePin)
                .onSuccess {
                    setState { copy(previousVault = null, restoring = false, restorePin = "") }
                    sendEffect(HomeEffect.VaultRestored)
                }
                .onFailure { setState { copy(restoring = false, restoreError = true, restorePin = "") } }
        }
    }
}
