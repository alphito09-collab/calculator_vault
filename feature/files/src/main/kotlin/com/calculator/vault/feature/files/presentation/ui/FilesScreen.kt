package com.calculator.vault.feature.files.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calculator.vault.core.designsystem.icon.VaultIcons
import com.calculator.vault.core.designsystem.preview.ScreenPreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.model.MediaType
import com.calculator.vault.core.model.VaultItem
import com.calculator.vault.core.ui.component.VaultItemRow
import com.calculator.vault.core.ui.gallery.GalleryEffects
import com.calculator.vault.core.ui.gallery.GalleryIntent
import com.calculator.vault.core.ui.gallery.GalleryScaffold
import com.calculator.vault.core.ui.gallery.GalleryState
import com.calculator.vault.core.ui.util.formatBytes
import com.calculator.vault.feature.files.R
import com.calculator.vault.feature.files.presentation.mvi.FilesViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun FilesRoute(onOpen: (VaultItem) -> Unit, onImport: () -> Unit, onBack: () -> Unit, viewModel: FilesViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    GalleryEffects(viewModel.effects, snackbar, onOpen, onImport, onBack)
    FilesScreen(state, viewModel::onIntent, snackbar)
}

@Composable
fun FilesScreen(state: GalleryState, onIntent: (GalleryIntent) -> Unit, snackbar: SnackbarHostState = remember { SnackbarHostState() }) {
    GalleryScaffold(
        state = state,
        onIntent = onIntent,
        emptyIcon = VaultIcons.File,
        emptyTitle = stringResource(R.string.files_empty_title),
        emptyMessage = stringResource(R.string.files_empty_message),
        importLabel = stringResource(R.string.files_import),
        snackbar = snackbar,
        showLayoutOptions = false,
    ) {
        LazyColumn(
            Modifier.align(Alignment.TopCenter).widthIn(max = 720.dp),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 130.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(state.items, key = { it.id }) { item ->
                VaultItemRow(
                    item = item,
                    subtitle = "${item.extension.uppercase().ifEmpty { "FILE" }} • ${formatBytes(item.sizeBytes)}",
                    onClick = { onIntent(GalleryIntent.Open(item)) },
                    onLongClick = { onIntent(GalleryIntent.Toggle(item)) },
                    selectionMode = state.selecting,
                    selected = item.id in state.selected,
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}

@ScreenPreviews
@Composable
private fun FilesPreview() = VaultPreview {
    val items = listOf("Lease agreement.pdf" to 2_100_000L, "Passport scan.jpg" to 340_000L, "Tax_2025.pdf" to 610_000L, "Contract_signed.txt" to 4_000L)
        .mapIndexed { i, (n, s) -> VaultItem("$i", MediaType.FILE, n, "application/pdf", s, null, null, 0, 0) }
    FilesScreen(GalleryState(MediaType.FILE, loading = false, items = items, selected = setOf("0", "1")), onIntent = {})
}
