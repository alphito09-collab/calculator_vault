package com.calculator.vault.feature.pin.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.calculator.vault.core.designsystem.preview.ThemePreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultTheme
import kotlin.math.roundToInt

/** Four underlined slots that show a blue "*" per typed digit; red and shaking on error. */
@Composable
fun PinSlots(filled: Int, isError: Boolean, errorTrigger: Int, modifier: Modifier = Modifier, length: Int = 4) {
    val shake = remember { Animatable(0f) }
    LaunchedEffect(errorTrigger) {
        if (errorTrigger > 0) for (t in listOf(-16f, 14f, -10f, 6f, 0f)) shake.animateTo(t, spring(stiffness = Spring.StiffnessHigh))
    }
    Row(modifier.offset { IntOffset(shake.value.roundToInt(), 0) }, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(length) { index ->
            val line by animateColorAsState(
                when {
                    isError -> VaultTheme.scheme.error
                    else -> VaultTheme.scheme.onBackground
                },
                label = "slotLine",
            )
            Column(Modifier.width(30.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                val starScale by animateFloatAsState(
                    if (index < filled) 1f else 0f,
                    spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "star",
                )
                Box(Modifier.height(34.dp), contentAlignment = Alignment.BottomCenter) {
                    Text(
                        "*",
                        style = VaultTheme.text.pinMask,
                        color = VaultTheme.scheme.primary,
                        modifier = Modifier.graphicsLayer {
                            scaleX = starScale
                            scaleY = starScale
                            alpha = starScale.coerceIn(0f, 1f)
                        },
                    )
                }
                Box(Modifier.padding(top = 2.dp).fillMaxWidth().height(2.dp).background(line))
            }
        }
    }
}

@ThemePreviews
@Composable
private fun PinSlotsPreview() = VaultPreview {
    Column(Modifier.padding(20.dp)) {
        PinSlots(filled = 2, isError = false, errorTrigger = 0)
        PinSlots(filled = 0, isError = true, errorTrigger = 0, modifier = Modifier.padding(top = 16.dp))
    }
}
