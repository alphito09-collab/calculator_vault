package com.calculator.vault.feature.photos.presentation.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calculator.vault.core.designsystem.component.VaultMenuItem
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
import com.calculator.vault.feature.photos.R
import com.calculator.vault.feature.photos.presentation.mvi.PhotosViewModel
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate

@Composable
fun PhotosRoute(
    onOpen: (VaultItem, Boolean) -> Unit,
    onImport: () -> Unit,
    onBack: () -> Unit,
    viewModel: PhotosViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    GalleryEffects(viewModel.effects, snackbar, onOpen = { onOpen(it, false) }, onImport = onImport, onBack = onBack)
    PhotosScreen(state, viewModel::onIntent, snackbar, onSlideshow = { state.items.firstOrNull()?.let { onOpen(it, true) } })
}

@Composable
fun PhotosScreen(
    state: GalleryState,
    onIntent: (GalleryIntent) -> Unit,
    snackbar: SnackbarHostState = remember { SnackbarHostState() },
    onSlideshow: () -> Unit = {},
) {
    GalleryScaffold(
        state = state,
        onIntent = onIntent,
        emptyIcon = VaultIcons.Photo,
        emptyTitle = stringResource(R.string.photos_empty_title),
        emptyMessage = stringResource(R.string.photos_empty_message),
        importLabel = stringResource(R.string.photos_import),
        snackbar = snackbar,
        extraMenuItems = {
            VaultMenuItem(stringResource(R.string.photos_slideshow), {
                onIntent(GalleryIntent.CloseMenu)
                onSlideshow()
            })
        },
    ) {
        SelectableMediaGrid(
            sections = if (state.groupByDate) state.sections else listOf(DateSection(LocalDate.MIN, state.items)),
            selectedIds = state.selected,
            onOpen = { onIntent(GalleryIntent.Open(it)) },
            onToggleSelect = { onIntent(GalleryIntent.Toggle(it)) },
            gridSize = state.gridSize,
            showHeaders = state.groupByDate,
        )
    }
}

@ScreenPreviews
@Composable
private fun PhotosPreview() = VaultPreview {
    val items = (1..10).map { VaultItem("$it", MediaType.PHOTO, "IMG_$it.jpg", "image/jpeg", 1, null, null, System.currentTimeMillis() - it * 86_400_000L / 3, 0) }
    PhotosScreen(
        GalleryState(MediaType.PHOTO, loading = false, items = items, sections = listOf(DateSection(LocalDate.now(), items.take(4)), DateSection(LocalDate.now().minusDays(5), items.drop(4))), selected = setOf("2")),
        onIntent = {},
    )
}

@ScreenPreviews
@Composable
private fun PhotosEmptyPreview() = VaultPreview { PhotosScreen(GalleryState(MediaType.PHOTO, loading = false), onIntent = {}) }
