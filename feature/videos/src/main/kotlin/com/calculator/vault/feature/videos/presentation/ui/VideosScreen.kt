package com.calculator.vault.feature.videos.presentation.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calculator.vault.core.designsystem.icon.VaultIcons
import com.calculator.vault.core.designsystem.preview.ScreenPreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.domain.usecase.DateSection
import com.calculator.vault.core.model.MediaType
import com.calculator.vault.core.model.VaultItem
import com.calculator.vault.core.ui.component.SelectableMediaGrid
import com.calculator.vault.core.ui.gallery.GalleryEffects
import com.calculator.vault.core.ui.gallery.GalleryIntent
import com.calculator.vault.core.ui.gallery.GalleryScaffold
import com.calculator.vault.core.ui.gallery.GalleryState
import com.calculator.vault.feature.videos.R
import com.calculator.vault.feature.videos.presentation.mvi.VideosViewModel
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate

@Composable
fun VideosRoute(onOpen: (VaultItem) -> Unit, onImport: () -> Unit, onBack: () -> Unit, viewModel: VideosViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    GalleryEffects(viewModel.effects, snackbar, onOpen, onImport, onBack)
    VideosScreen(state, viewModel::onIntent, snackbar)
}

@Composable
fun VideosScreen(state: GalleryState, onIntent: (GalleryIntent) -> Unit, snackbar: SnackbarHostState = remember { SnackbarHostState() }) {
    GalleryScaffold(
        state = state,
        onIntent = onIntent,
        emptyIcon = VaultIcons.Video,
        emptyTitle = stringResource(R.string.videos_empty_title),
        emptyMessage = stringResource(R.string.videos_empty_message),
        importLabel = stringResource(R.string.videos_import),
        snackbar = snackbar,
    ) {
        SelectableMediaGrid(
            sections = if (state.groupByDate) state.sections else listOf(DateSection(LocalDate.MIN, state.items)),
            selectedIds = state.selected,
            onOpen = { onIntent(GalleryIntent.Open(it)) },
            onToggleSelect = { onIntent(GalleryIntent.Toggle(it)) },
            gridSize = state.gridSize,
            showHeaders = state.groupByDate,
            showDuration = true,
        )
    }
}

@ScreenPreviews
@Composable
private fun VideosPreview() = VaultPreview {
    val items = (1..7).map { VaultItem("$it", MediaType.VIDEO, "VID_$it.mp4", "video/mp4", 1, null, null, 0, 0, durationMs = it * 17_000L) }
    VideosScreen(GalleryState(MediaType.VIDEO, loading = false, items = items, groupByDate = false), onIntent = {})
}
