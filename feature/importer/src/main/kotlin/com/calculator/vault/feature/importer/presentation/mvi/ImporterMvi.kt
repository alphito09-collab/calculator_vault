package com.calculator.vault.feature.importer.presentation.mvi

import androidx.lifecycle.viewModelScope
import com.calculator.vault.core.domain.usecase.HideMediaUseCase
import com.calculator.vault.core.model.DeviceAlbum
import com.calculator.vault.core.model.DeviceMedia
import com.calculator.vault.core.model.MediaType
import com.calculator.vault.core.model.TransferProgress
import com.calculator.vault.core.ui.mvi.MviViewModel
import com.calculator.vault.core.ui.mvi.UiEffect
import com.calculator.vault.core.ui.mvi.UiIntent
import com.calculator.vault.core.ui.mvi.UiState
import com.calculator.vault.feature.importer.domain.usecase.LoadDeviceAlbumsUseCase
import com.calculator.vault.feature.importer.domain.usecase.MediaAccessUseCase
import com.calculator.vault.feature.importer.domain.usecase.ResolvePickedDocumentsUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam

data class ImporterState(
    /** The type this importer was opened for; null = "All" with filter chips. */
    val lockedType: MediaType? = null,
    val filter: MediaType? = null,
    val albums: List<DeviceAlbum> = emptyList(),
    val selected: Map<String, DeviceMedia> = emptyMap(),
    val loading: Boolean = true,
    /** No permission to list this type; an empty list then offers "Allow access". */
    val needsPermission: Boolean = false,
    val confirmHide: Boolean = false,
    val progress: TransferProgress? = null,
) : UiState {
    val visibleItems: List<DeviceMedia> get() = albums.flatMap { it.items }
    val allSelected: Boolean get() = visibleItems.isNotEmpty() && visibleItems.all { it.uri in selected }
    val selectionType: MediaType? get() = selected.values.map { it.type }.distinct().singleOrNull()
}

sealed interface ImporterIntent : UiIntent {
    data class Filter(val type: MediaType?) : ImporterIntent
    data class Toggle(val item: DeviceMedia) : ImporterIntent
    data object ToggleSelectAll : ImporterIntent
    data class DocumentsPicked(val uris: List<String>) : ImporterIntent
    /** [permanentlyDenied]: Android will no longer show the prompt, so only app settings can help. */
    data class PermissionResult(val permanentlyDenied: Boolean) : ImporterIntent
    data object AllowAccess : ImporterIntent
    data object Resumed : ImporterIntent
    data object Hide : ImporterIntent
    data object ConfirmHide : ImporterIntent
    data object DismissHide : ImporterIntent
    data object Close : ImporterIntent
}

sealed interface ImporterEffect : UiEffect {
    data class Done(val hidden: Int, val failed: Int, val type: MediaType?) : ImporterEffect
    data class RequestPermission(val permissions: List<String>) : ImporterEffect
    data object OpenAppSettings : ImporterEffect
    data object OpenAllFilesSettings : ImporterEffect
    data object Close : ImporterEffect
}

@KoinViewModel
class ImporterViewModel(
    @InjectedParam type: MediaType?,
    private val loadAlbums: LoadDeviceAlbumsUseCase,
    private val resolveDocuments: ResolvePickedDocumentsUseCase,
    private val mediaAccess: MediaAccessUseCase,
    private val hideMedia: HideMediaUseCase,
) : MviViewModel<ImporterState, ImporterIntent, ImporterEffect>(ImporterState(lockedType = type, filter = type)) {

    /**
     * The permission prompt opens by itself at most once per visit. Asking again on every empty
     * result looped forever (prompt → instant answer → reload → prompt) whenever access was
     * already granted but the device simply had nothing of that type.
     */
    private var autoAsked = false
    private var permanentlyDenied = false
    private var loadJob: Job? = null

    init {
        load()
    }

    override fun handleIntent(intent: ImporterIntent) {
        when (intent) {
            is ImporterIntent.Filter -> {
                setState { copy(filter = intent.type) }
                load()
            }
            is ImporterIntent.Toggle -> setState {
                copy(selected = if (intent.item.uri in selected) selected - intent.item.uri else selected + (intent.item.uri to intent.item))
            }
            ImporterIntent.ToggleSelectAll -> setState {
                copy(selected = if (allSelected) selected - visibleItems.map { it.uri }.toSet() else selected + visibleItems.associateBy { it.uri })
            }
            is ImporterIntent.DocumentsPicked -> viewModelScope.launch {
                val picked = resolveDocuments(intent.uris)
                if (picked.isNotEmpty()) {
                    setState { copy(selected = selected + picked.associateBy { it.uri }, confirmHide = true) }
                }
            }
            is ImporterIntent.PermissionResult -> {
                permanentlyDenied = intent.permanentlyDenied
                load()
            }
            ImporterIntent.AllowAccess -> askForAccess()
            // Back from system settings: list what the new access reveals.
            ImporterIntent.Resumed -> if (currentState.needsPermission && mediaAccess.hasAccess(currentState.filter)) load()
            ImporterIntent.Hide -> if (currentState.selected.isNotEmpty()) setState { copy(confirmHide = true) }
            ImporterIntent.DismissHide -> setState { copy(confirmHide = false) }
            ImporterIntent.ConfirmHide -> hide()
            ImporterIntent.Close -> sendEffect(ImporterEffect.Close)
        }
    }

    private fun load() {
        val filter = currentState.filter
        val hasAccess = mediaAccess.hasAccess(filter)
        setState { copy(loading = true, needsPermission = !hasAccess) }
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val albums = loadAlbums(filter)
            setState { copy(albums = albums, loading = false) }
        }
        // Documents need "All files access" in system settings; never leave for it unasked.
        if (!hasAccess && !autoAsked && mediaAccess.permissions(filter).isNotEmpty()) {
            autoAsked = true
            askForAccess()
        }
    }

    private fun askForAccess() {
        val permissions = mediaAccess.permissions(currentState.filter)
        sendEffect(
            when {
                permissions.isEmpty() -> ImporterEffect.OpenAllFilesSettings
                permanentlyDenied -> ImporterEffect.OpenAppSettings
                else -> ImporterEffect.RequestPermission(permissions)
            },
        )
    }

    private fun hide() {
        val sources = currentState.selected.values.toList()
        val type = currentState.selectionType
        setState { copy(confirmHide = false, progress = TransferProgress(0, sources.size)) }
        viewModelScope.launch {
            var last = TransferProgress(0, sources.size)
            hideMedia(sources).onEach { last = it; setState { copy(progress = it) } }.collect()
            setState { copy(progress = null, selected = emptyMap()) }
            sendEffect(ImporterEffect.Done(last.completed, last.failed, type))
        }
    }
}
