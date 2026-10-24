package com.calculator.vault.core.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.calculator.vault.core.designsystem.theme.VaultRadius
import com.calculator.vault.core.designsystem.theme.VaultTheme
import com.calculator.vault.core.domain.usecase.DateSection
import com.calculator.vault.core.model.GridSize
import com.calculator.vault.core.model.VaultItem
import com.calculator.vault.core.ui.util.formatSectionDate
import androidx.compose.ui.draw.clip

/** Minimum cell width per grid density; columns adapt to window width (landscape, tablets). */
fun GridSize.minCellSize(): Dp = when (this) {
    GridSize.LARGE -> 150.dp
    GridSize.MEDIUM -> 100.dp
    GridSize.SMALL -> 74.dp
}

/**
 * Photos/videos grid with optional "Today / 17 sep" headers. Tap opens (or toggles while
 * selecting); long-press starts selection.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SelectableMediaGrid(
    sections: List<DateSection>,
    selectedIds: Set<String>,
    onOpen: (VaultItem) -> Unit,
    onToggleSelect: (VaultItem) -> Unit,
    modifier: Modifier = Modifier,
    gridSize: GridSize = GridSize.MEDIUM,
    showHeaders: Boolean = true,
    showDuration: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 120.dp),
    state: LazyGridState = rememberLazyGridState(),
) {
    val haptics = LocalHapticFeedback.current
    val selecting = selectedIds.isNotEmpty()
    LazyVerticalGrid(
        columns = GridCells.Adaptive(gridSize.minCellSize()),
        modifier = modifier,
        state = state,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        sections.forEach { section ->
            if (showHeaders) {
                item(key = "header-${section.date}", span = { GridItemSpan(maxLineSpan) }, contentType = "header") {
                    Text(
                        formatSectionDate(section.date),
                        style = VaultTheme.typography.titleSmall.copy(fontSize = VaultTheme.typography.bodyLarge.fontSize),
                        color = VaultTheme.scheme.onBackground,
                        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp).animateItem(),
                    )
                }
            }
            items(section.items, key = { it.id }, contentType = { "media" }) { item ->
                MediaThumbnail(
                    itemId = item.id,
                    model = if (item.hasThumbnail) com.calculator.vault.core.model.VaultThumbnailRequest(item.id) else null,
                    durationMs = if (showDuration) item.durationMs ?: 0L else null,
                    selectionMode = selecting,
                    selected = item.id in selectedIds,
                    modifier = Modifier
                        .animateItem()
                        .clip(VaultRadius.Thumbnail)
                        .combinedClickable(
                            onClick = { if (selecting) onToggleSelect(item) else onOpen(item) },
                            onLongClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onToggleSelect(item)
                            },
                        ),
                )
            }
        }
    }
}
