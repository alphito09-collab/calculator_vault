package com.calculator.vault.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import com.calculator.vault.core.designsystem.R
import com.calculator.vault.core.designsystem.icon.VaultIcons
import com.calculator.vault.core.designsystem.preview.ThemePreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultRadius
import com.calculator.vault.core.designsystem.theme.VaultTheme
import kotlin.math.roundToInt

/**
 * Rounded keypad key with ripple, haptic tick and a springy press scale. Used by both the
 * calculator and the PIN pad.
 */
@Composable
fun KeypadKey(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    container: Color = VaultTheme.colors.keyDigit,
    contentColor: Color = VaultTheme.colors.onKey,
    onLongClick: (() -> Unit)? = null,
    contentDescription: String? = null,
    content: @Composable () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val scale = rememberPressScale(interaction, pressedScale = 0.93f)
    val haptics = LocalHapticFeedback.current
    Box(
        modifier = modifier
            .scaleOnPress(scale)
            .clip(VaultRadius.Key)
            .background(container)
            .then(
                if (onLongClick != null) {
                    Modifier.combinedClickableCompat(interaction, onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove); onClick()
                    }, onLongClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress); onLongClick()
                    })
                } else {
                    Modifier.clickable(interaction, ripple(color = contentColor), role = Role.Button) {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onClick()
                    }
                },
            )
            .then(if (contentDescription != null) Modifier.semantics { this.contentDescription = contentDescription } else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.material3.LocalContentColor provides contentColor,
        ) { content() }
    }
}

@Composable
private fun Modifier.combinedClickableCompat(
    interaction: MutableInteractionSource,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
): Modifier = combinedClickable(
    interactionSource = interaction,
    indication = ripple(),
    role = Role.Button,
    onClick = onClick,
    onLongClick = onLongClick,
)

@Composable
fun KeyLabel(text: String, style: TextStyle = VaultTheme.text.calculatorKey) {
    Text(text, style = style, color = androidx.compose.material3.LocalContentColor.current)
}

/** Four (or [length]) dots; filled in blue, red when [isError], shakes on error. */
@Composable
fun PinDots(
    filled: Int,
    modifier: Modifier = Modifier,
    length: Int = 4,
    isError: Boolean = false,
    errorTrigger: Int = 0,
) {
    val shake = remember { Animatable(0f) }
    LaunchedEffect(errorTrigger) {
        if (errorTrigger > 0) {
            for (target in listOf(-18f, 16f, -12f, 8f, -4f, 0f)) {
                shake.animateTo(target, spring(stiffness = Spring.StiffnessHigh))
            }
        }
    }
    Row(
        modifier.offset { IntOffset(shake.value.roundToInt(), 0) },
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        repeat(length) { index ->
            val isFilled = index < filled
            val color by animateColorAsState(
                when {
                    isError -> VaultTheme.scheme.error
                    isFilled -> VaultTheme.scheme.primary
                    else -> Color.Transparent
                },
                label = "dot",
            )
            val dotScale by animateFloatAsState(
                if (isFilled || isError) 1f else 0.9f,
                spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "dotScale",
            )
            Box(
                Modifier
                    .size(14.dp * dotScale)
                    .clip(CircleShape)
                    .background(color)
                    .border(1.dp, if (isFilled || isError) Color.Transparent else VaultTheme.colors.iconTileContent, CircleShape),
            )
        }
    }
}

/** 3×4 numeric PIN pad: 1-9, blank, 0, backspace. Keys keep a square-ish shape at any width. */
@Composable
fun PinPad(
    onDigit: (Int) -> Unit,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier,
    onBackspaceLongPress: (() -> Unit)? = null,
    bottomStart: (@Composable () -> Unit)? = null,
) {
    BoxWithConstraints(modifier.widthIn(max = 360.dp)) {
        val gap = 16.dp
        val rows = listOf(listOf(1, 2, 3), listOf(4, 5, 6), listOf(7, 8, 9))
        Column(verticalArrangement = Arrangement.spacedBy(gap)) {
            rows.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                    row.forEach { digit ->
                        KeypadKey(onClick = { onDigit(digit) }, modifier = Modifier.weight(1f).aspectRatio(1.18f)) {
                            KeyLabel(digit.toString(), VaultTheme.text.pinKey)
                        }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(gap), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f).aspectRatio(1.18f), contentAlignment = Alignment.Center) { bottomStart?.invoke() }
                KeypadKey(onClick = { onDigit(0) }, modifier = Modifier.weight(1f).aspectRatio(1.18f)) {
                    KeyLabel("0", VaultTheme.text.pinKey)
                }
                KeypadKey(
                    onClick = onBackspace,
                    onLongClick = onBackspaceLongPress,
                    modifier = Modifier.weight(1f).aspectRatio(1.18f),
                    container = Color.Transparent,
                    contentDescription = stringResource(R.string.ds_cd_backspace),
                ) {
                    Icon(painterResource(VaultIcons.Backspace), null, modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

/**
 * 3×3 pattern lock. Emits the visited dot indices (0..8) when the finger lifts.
 * [isError] tints the drawn path red; [resetKey] clears it.
 */
@Composable
fun PatternLock(
    onPatternComplete: (List<Int>) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    resetKey: Any? = null,
) {
    val path = remember(resetKey) { mutableStateListOf<Int>() }
    var finger by remember(resetKey) { mutableStateOf<Offset?>(null) }
    val haptics = LocalHapticFeedback.current
    val lineColor = if (isError) VaultTheme.scheme.error else VaultTheme.scheme.primary
    val dotColor = VaultTheme.colors.iconTileContent
    BoxWithConstraints(modifier.fillMaxWidth().aspectRatio(1f).widthIn(max = 340.dp)) {
        val sizePx = constraints.maxWidth.toFloat()
        val cell = sizePx / 3f
        fun center(i: Int) = Offset(cell * (i % 3) + cell / 2f, cell * (i / 3) + cell / 2f)
        fun hit(p: Offset): Int? {
            for (i in 0 until 9) {
                if ((center(i) - p).getDistance() < cell * 0.3f) return i
            }
            return null
        }
        Canvas(
            Modifier
                .fillMaxSize()
                .pointerInput(resetKey) {
                    detectDragGestures(
                        onDragStart = { start ->
                            path.clear()
                            hit(start)?.let { path.add(it); haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove) }
                            finger = start
                        },
                        onDrag = { change, _ ->
                            finger = change.position
                            val idx = hit(change.position)
                            if (idx != null && idx !in path) {
                                // Auto-include the skipped middle dot (e.g. 0 → 2 passes 1).
                                path.lastOrNull()?.let { last ->
                                    val mid = midpoint(last, idx)
                                    if (mid != null && mid !in path) path.add(mid)
                                }
                                path.add(idx)
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        },
                        onDragEnd = {
                            finger = null
                            if (path.isNotEmpty()) onPatternComplete(path.toList())
                        },
                        onDragCancel = { finger = null },
                    )
                },
        ) {
            val stroke = 6.dp.toPx()
            for (i in 1 until path.size) {
                drawLine(lineColor.copy(alpha = 0.7f), center(path[i - 1]), center(path[i]), stroke, StrokeCap.Round)
            }
            val last = path.lastOrNull()
            val f = finger
            if (last != null && f != null) drawLine(lineColor.copy(alpha = 0.45f), center(last), f, stroke, StrokeCap.Round)
            for (i in 0 until 9) {
                val selected = i in path
                drawCircle(if (selected) lineColor else dotColor, radius = if (selected) 9.dp.toPx() else 7.dp.toPx(), center = center(i))
                if (selected) {
                    drawCircle(lineColor.copy(alpha = 0.18f), radius = 22.dp.toPx(), center = center(i))
                    drawCircle(lineColor.copy(alpha = 0.4f), radius = 22.dp.toPx(), center = center(i), style = Stroke(1.dp.toPx()))
                }
            }
        }
    }
}

private fun midpoint(a: Int, b: Int): Int? {
    val (ax, ay) = a % 3 to a / 3
    val (bx, by) = b % 3 to b / 3
    if ((ax + bx) % 2 != 0 || (ay + by) % 2 != 0) return null
    val mid = ((ay + by) / 2) * 3 + (ax + bx) / 2
    return mid.takeIf { it != a && it != b }
}

@ThemePreviews
@Composable
private fun KeypadsPreview() = VaultPreview {
    Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        PinDots(filled = 3)
        Spacer(Modifier.size(24.dp))
        PinPad(onDigit = {}, onBackspace = {})
    }
}

@ThemePreviews
@Composable
private fun PatternPreview() = VaultPreview {
    PatternLock(onPatternComplete = {}, modifier = Modifier.padding(32.dp))
}
