package com.calculator.vault.feature.photos.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import coil3.compose.AsyncImage
import com.calculator.vault.core.model.VaultImageRequest
import com.calculator.vault.core.model.VaultThumbnailRequest
import kotlinx.coroutines.launch

/**
 * Pinch/double-tap zoom with panning. Gestures are only consumed while zoomed (or with two
 * fingers), so the surrounding pager can still swipe between photos at 1×.
 */
@Composable
fun ZoomableImage(itemId: String, hasThumbnail: Boolean, onTap: () -> Unit, modifier: Modifier = Modifier) {
    val scale = remember { Animatable(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var size by remember { mutableStateOf(IntSize.Zero) }
    val scope = rememberCoroutineScope()

    fun clamp(raw: Offset, s: Float): Offset {
        val maxX = (size.width * (s - 1f)) / 2f
        val maxY = (size.height * (s - 1f)) / 2f
        return Offset(raw.x.coerceIn(-maxX, maxX), raw.y.coerceIn(-maxY, maxY))
    }

    Box(
        modifier
            .fillMaxSize()
            .clipToBounds()
            .onSizeChanged { size = it }
            .pointerInput(itemId) {
                detectTapGestures(
                    onTap = { onTap() },
                    onDoubleTap = { tap ->
                        scope.launch {
                            if (scale.value > 1.05f) {
                                offset = Offset.Zero
                                scale.animateTo(1f, spring())
                            } else {
                                val target = 2.5f
                                val center = Offset(size.width / 2f, size.height / 2f)
                                offset = clamp((center - tap) * (target - 1f), target)
                                scale.animateTo(target, spring())
                            }
                        }
                    },
                )
            }
            .pointerInput(itemId) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    do {
                        val event = awaitPointerEvent()
                        val multiTouch = event.changes.count { it.pressed } > 1
                        if (multiTouch || scale.value > 1.01f) {
                            val newScale = (scale.value * event.calculateZoom()).coerceIn(1f, 5f)
                            scope.launch { scale.snapTo(newScale) }
                            offset = clamp(offset + event.calculatePan(), newScale)
                            event.changes.forEach { if (it.positionChanged()) it.consume() }
                        }
                    } while (event.changes.any { it.pressed })
                    if (scale.value < 1.01f) offset = Offset.Zero
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        val layer = Modifier.fillMaxSize().graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
            translationX = offset.x
            translationY = offset.y
        }
        if (hasThumbnail) AsyncImage(VaultThumbnailRequest(itemId), null, layer, contentScale = ContentScale.Fit)
        AsyncImage(VaultImageRequest(itemId), null, layer, contentScale = ContentScale.Fit)
    }
}
