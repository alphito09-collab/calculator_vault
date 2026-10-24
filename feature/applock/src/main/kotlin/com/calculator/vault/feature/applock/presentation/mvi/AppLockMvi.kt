package com.calculator.vault.feature.applock.presentation.mvi

import androidx.lifecycle.viewModelScope
import com.calculator.vault.core.model.AppLockAuthMethod
import com.calculator.vault.core.model.AppLockSettings
import com.calculator.vault.core.model.InstalledApp
import com.calculator.vault.core.model.LockAfter
import com.calculator.vault.core.ui.mvi.MviViewModel
import com.calculator.vault.core.ui.mvi.UiEffect
import com.calculator.vault.core.ui.mvi.UiIntent
import com.calculator.vault.core.ui.mvi.UiState
import com.calculator.vault.feature.applock.domain.repository.AppLockPermissions
import com.calculator.vault.feature.applock.domain.usecase.AppLockPermissionsUseCase
import com.calculator.vault.feature.applock.domain.usecase.LoadInstalledAppsUseCase
import com.calculator.vault.feature.applock.domain.usecase.ObserveAppLockSettingsUseCase
import com.calculator.vault.feature.applock.domain.usecase.ToggleAppLockedUseCase
import com.calculator.vault.feature.applock.domain.usecase.UpdateAppLockSettingsUseCase
import com.calculator.vault.feature.applock.service.AppLockController
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

// region App list
data class AppLockListState(
    val loading: Boolean = true,
    val apps: List<InstalledApp> = emptyList(),
    val query: String = "",
    val searching: Boolean = false,
    val permissions: AppLockPermissions = AppLockPermissions(true, true, true),
    val enabled: Boolean = false,
    val showHint: Boolean = false,
) : UiState {
    val visibleApps: List<InstalledApp>
        get() = if (query.isBlank()) apps else apps.filter { it.label.contains(query.trim(), ignoreCase = true) }
}

sealed interface AppLockListIntent : UiIntent {
    data class Toggle(val app: InstalledApp) : AppLockListIntent
    data class Query(val value: String) : AppLockListIntent
    data object ToggleSearch : AppLockListIntent
    data object DismissHint : AppLockListIntent
    data object Resumed : AppLockListIntent
    data object GrantUsageAccess : AppLockListIntent
    data object GrantOverlay : AppLockListIntent
    data object OpenSettings : AppLockListIntent
    data object Back : AppLockListIntent
}

sealed interface AppLockListEffect : UiEffect {
    data class Locked(val label: String) : AppLockListEffect
    data object OpenUsageAccess : AppLockListEffect
    data object OpenOverlay : AppLockListEffect
    data object OpenSettings : AppLockListEffect
    data object Close : AppLockListEffect
}

@KoinViewModel
class AppLockListViewModel(
    observeSettings: ObserveAppLockSettingsUseCase,
    private val loadApps: LoadInstalledAppsUseCase,
    private val toggleLocked: ToggleAppLockedUseCase,
    private val permissions: AppLockPermissionsUseCase,
    private val controller: AppLockController,
) : MviViewModel<AppLockListState, AppLockListIntent, AppLockListEffect>(AppLockListState()) {

    init {
        observeSettings().onEach { settings ->
            setState {
                copy(
                    enabled = settings.enabled,
                    showHint = loading && settings.lockedPackages.isEmpty(),
                    apps = apps.map { it.copy(isLocked = it.packageName in settings.lockedPackages) },
                )
            }
        }.launchIn(viewModelScope)
        viewModelScope.launch {
            val apps = loadApps()
            setState { copy(loading = false, apps = apps) }
        }
    }

    override fun handleIntent(intent: AppLockListIntent) {
        when (intent) {
            is AppLockListIntent.Toggle -> viewModelScope.launch {
                val lock = !intent.app.isLocked
                toggleLocked(intent.app.packageName, lock)
                setState { copy(showHint = false) }
                if (lock) sendEffect(AppLockListEffect.Locked(intent.app.label))
            }
            is AppLockListIntent.Query -> setState { copy(query = intent.value) }
            AppLockListIntent.ToggleSearch -> setState { copy(searching = !searching, query = "") }
            AppLockListIntent.DismissHint -> setState { copy(showHint = false) }
            AppLockListIntent.Resumed -> {
                setState { copy(permissions = this@AppLockListViewModel.permissions()) }
                controller.refresh()
            }
            AppLockListIntent.GrantUsageAccess -> sendEffect(AppLockListEffect.OpenUsageAccess)
            AppLockListIntent.GrantOverlay -> sendEffect(AppLockListEffect.OpenOverlay)
            AppLockListIntent.OpenSettings -> sendEffect(AppLockListEffect.OpenSettings)
            AppLockListIntent.Back -> if (currentState.searching) setState { copy(searching = false, query = "") } else sendEffect(AppLockListEffect.Close)
        }
    }
}
// endregion

// region Settings
data class AppLockSettingsState(val settings: AppLockSettings = AppLockSettings(), val fingerprintAvailable: Boolean = true) : UiState

sealed interface AppLockSettingsIntent : UiIntent {
    data class Enabled(val enabled: Boolean) : AppLockSettingsIntent
    data class LockAfterChanged(val value: LockAfter) : AppLockSettingsIntent
    data class Method(val method: AppLockAuthMethod) : AppLockSettingsIntent
    data object SetupPattern : AppLockSettingsIntent
    data object Back : AppLockSettingsIntent
}

sealed interface AppLockSettingsEffect : UiEffect {
    data object OpenPatternSetup : AppLockSettingsEffect
    data object Close : AppLockSettingsEffect
}

@KoinViewModel
class AppLockSettingsViewModel(
    observeSettings: ObserveAppLockSettingsUseCase,
    private val update: UpdateAppLockSettingsUseCase,
) : MviViewModel<AppLockSettingsState, AppLockSettingsIntent, AppLockSettingsEffect>(AppLockSettingsState()) {

    init {
        observeSettings().onEach { setState { copy(settings = it) } }.launchIn(viewModelScope)
    }

    override fun handleIntent(intent: AppLockSettingsIntent) {
        when (intent) {
            is AppLockSettingsIntent.Enabled -> viewModelScope.launch { update.setEnabled(intent.enabled) }
            is AppLockSettingsIntent.LockAfterChanged -> viewModelScope.launch { update.setLockAfter(intent.value) }
            is AppLockSettingsIntent.Method -> if (intent.method == AppLockAuthMethod.PATTERN && !currentState.settings.hasPattern) {
                sendEffect(AppLockSettingsEffect.OpenPatternSetup)
            } else {
                viewModelScope.launch { update.setMethod(intent.method) }
            }
            AppLockSettingsIntent.SetupPattern -> sendEffect(AppLockSettingsEffect.OpenPatternSetup)
            AppLockSettingsIntent.Back -> sendEffect(AppLockSettingsEffect.Close)
        }
    }
}
// endregion
