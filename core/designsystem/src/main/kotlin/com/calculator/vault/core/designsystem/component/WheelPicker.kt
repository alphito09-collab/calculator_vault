package com.calculator.vault.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.calculator.vault.core.designsystem.preview.ThemePreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultTheme
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sign

/**
 * Horizontal snapping wheel. The centred value sits on a highlighted pill while its neighbours
 * shrink, fade and turn away like a drum, drifting toward the centre (parallax). Swipe or tap a
 * value; each step ticks the haptics and [onSelectedChange] fires once the wheel settles.
 */
@Composable
fun <T> WheelPicker(
    items: List<T>,
    selected: T,
    onSelectedChange: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    itemWidth: Dp = 64.dp,
    height: Dp = 72.dp,
    label: (T) -> String = { it.toString() },
) {
    val selectedIndex = items.indexOf(selected).coerceAtLeast(0)
    val state = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex)
    val centeredIndex by remember(state) { derivedStateOf { state.centeredItemIndex() } }
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val currentItems by rememberUpdatedState(items)
    val currentSelected by rememberUpdatedState(selected)
    val currentOnChange by rememberUpdatedState(onSelectedChange)

    LaunchedEffect(state) {
        snapshotFlow { centeredIndex }.drop(1).collect {
            if (state.isScrollInProgress) haptics.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
        }
    }
    LaunchedEffect(state) {
        snapshotFlow { state.isScrollInProgress }.filter { !it }.collect {
            currentItems.getOrNull(centeredIndex)?.takeIf { it != currentSelected }?.let(currentOnChange)
        }
    }
    // Follow outside changes, e.g. the stored value arriving after first composition.
    LaunchedEffect(selectedIndex) {
        if (!state.isScrollInProgress && centeredIndex != selectedIndex) state.animateScrollToCenter(selectedIndex)
    }

    BoxWithConstraints(modifier.fillMaxWidth().height(height).alpha(if (enabled) 1f else 0.45f), contentAlignment = Alignment.Center) {
        val side = ((maxWidth - itemWidth) / 2).coerceAtLeast(0.dp)
        Box(
            Modifier
                .size(itemWidth, height - 12.dp)
                .clip(VaultTheme.shapes.large)
                .background(VaultTheme.scheme.primaryContainer),
        )
        LazyRow(
            state = state,
            contentPadding = PaddingValues(horizontal = side),
            flingBehavior = rememberSnapFlingBehavior(state, SnapPosition.Center),
            userScrollEnabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithContent {
                    drawContent()
                    drawRect(EdgeFade, blendMode = BlendMode.DstIn)
                },
        ) {
            itemsIndexed(items) { index, item ->
                val isCenter = index == centeredIndex
                val color by animateColorAsState(
                    if (isCenter) VaultTheme.scheme.primary else VaultTheme.scheme.onSurfaceVariant,
                    label = "wheelItemColor",
                )
                Box(
                    Modifier
                        .width(itemWidth)
                        .fillMaxHeight()
                        .graphicsLayer {
                            val info = state.layoutInfo
                            val me = info.visibleItemsInfo.firstOrNull { it.index == index } ?: return@graphicsLayer
                            val center = (info.viewportStartOffset + info.viewportEndOffset) / 2f
                            // Distance from the centre in items; negative = before it. Mirrored for RTL.
                            val logical = ((me.offset + me.size / 2f - center) / me.size).coerceIn(-3f, 3f)
                            val f = if (rtl) -logical else logical
                            val d = abs(f)
                            val scale = 1f - 0.2f * d.coerceAtMost(2f)
                            scaleX = scale
                            scaleY = scale
                            alpha = (1f - 0.3f * d).coerceIn(0.15f, 1f)
                            // Capped well below 90°: an edge-on layer has no inverse, and touches
                            // anywhere would hit-test into it.
                            rotationY = -f.coerceIn(-MAX_TURN_STEPS, MAX_TURN_STEPS) * DEGREES_PER_STEP
                            translationX = -sign(f) * d.coerceAtMost(2f) * d.coerceAtMost(2f) * size.width * 0.06f
                            cameraDistance = 10f * density
                        }
                        .clip(VaultTheme.shapes.large)
                        .selectable(
                            selected = isCenter,
                            enabled = enabled,
                            role = Role.RadioButton,
                            onClick = { scope.launch { state.animateScrollToCenter(index) } },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(label(item), style = VaultTheme.text.stat, color = color)
                }
            }
        }
    }
}

/** Scrolls so that item [index] sits exactly on the viewport centre. */
private suspend fun LazyListState.animateScrollToCenter(index: Int) {
    val visible = layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }
    if (visible == null) {
        scrollToItem(index)
        return
    }
    val info = layoutInfo
    val center = (info.viewportStartOffset + info.viewportEndOffset) / 2
    animateScrollBy((visible.offset + visible.size / 2 - center).toFloat())
}

/** Index of the item whose centre is closest to the viewport centre. */
private fun LazyListState.centeredItemIndex(): Int {
    val info = layoutInfo
    val center = (info.viewportStartOffset + info.viewportEndOffset) / 2
    return info.visibleItemsInfo.minByOrNull { abs(it.offset + it.size / 2 - center) }?.index ?: firstVisibleItemIndex
}

private const val DEGREES_PER_STEP = 30f
private const val MAX_TURN_STEPS = 1.8f

private val EdgeFade = Brush.horizontalGradient(0f to Color.Transparent, 0.25f to Color.Black, 0.75f to Color.Black, 1f to Color.Transparent)

@ThemePreviews
@Composable
private fun WheelPickerPreview() = VaultPreview {
    var value by remember { mutableIntStateOf(3) }
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        WheelPicker(items = (1..6).toList(), selected = value, onSelectedChange = { value = it })
        WheelPicker(items = (1..6).toList(), selected = 5, onSelectedChange = {}, enabled = false)
    }
}
