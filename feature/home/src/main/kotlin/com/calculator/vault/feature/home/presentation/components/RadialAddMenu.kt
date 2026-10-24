package com.calculator.vault.feature.home.presentation.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.calculator.vault.core.designsystem.component.BottomBarFabCenterFromBottom
import com.calculator.vault.core.designsystem.component.VaultFab
import com.calculator.vault.core.designsystem.icon.VaultIcons
import com.calculator.vault.core.designsystem.preview.ThemePreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultTheme
import com.calculator.vault.feature.home.presentation.mvi.AddAction
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

private const val ARC_START = 165.0
private const val ARC_END = 15.0

data class AddOption(val action: AddAction, @DrawableRes val icon: Int, val label: String, val tint: Color)

/** The "+" that turns into "×" while the radial menu is open. */
@Composable
fun AddFab(open: Boolean, onClick: () -> Unit, contentDescription: String) {
    val rotation by animateFloatAsState(if (open) 135f else 0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "fabRotation")
    VaultFab(onClick = onClick, contentDescription = contentDescription, modifier = Modifier.graphicsLayer { rotationZ = rotation })
}

/**
 * Dimmed overlay with the add options fanned out in a semicircle above the "+" button.
 * Each option springs out from the button with a small stagger.
 */
@Composable
fun RadialAddMenu(open: Boolean, options: List<AddOption>, onSelect: (AddAction) -> Unit, onDismiss: () -> Unit) {
    val scrimAlpha by animateFloatAsState(if (open) 1f else 0f, tween(220), label = "scrim")
    if (scrimAlpha == 0f && !open) return
    val progress = remember(options) { options.map { Animatable(0f) } }
    LaunchedEffect(open) {
        progress.forEachIndexed { index, anim ->
            launch {
                delay((if (open) index else options.lastIndex - index) * 35L)
                anim.animateTo(
                    if (open) 1f else 0f,
                    if (open) spring(dampingRatio = 0.62f, stiffness = Spring.StiffnessMediumLow) else tween(140),
                )
            }
        }
    }
    Box(
        Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = scrimAlpha }
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable(remember { MutableInteractionSource() }, indication = null, enabled = open, onClick = onDismiss),
    ) {
        BoxWithConstraints(Modifier.fillMaxSize().navigationBarsPadding()) {
            val density = LocalDensity.current
            val itemWidth = 76.dp
            val circle = 56.dp
            val radius = min(maxWidth.value * 0.38f, maxHeight.value * 0.45f).coerceAtMost(140f).dp
            with(density) {
                val fabX = maxWidth.toPx() / 2f
                val fabY = (maxHeight - BottomBarFabCenterFromBottom).toPx()
                options.forEachIndexed { index, option ->
                    // Fan from 165° to 15° so the outer options clear the bottom bar.
                    val angle = Math.toRadians(ARC_START - index * ((ARC_START - ARC_END) / (options.size - 1)))
                    val p = progress[index].value
                    val cx = fabX + (radius.toPx() * cos(angle)).toFloat() * p
                    val cy = fabY - (radius.toPx() * sin(angle)).toFloat() * p
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(itemWidth)
                            .offset { IntOffset((cx - itemWidth.toPx() / 2f).roundToInt(), (cy - circle.toPx() / 2f).roundToInt()) }
                            .graphicsLayer {
                                alpha = p.coerceIn(0f, 1f)
                                scaleX = 0.4f + 0.6f * p
                                scaleY = 0.4f + 0.6f * p
                            },
                    ) {
                        Box(
                            Modifier
                                .size(circle)
                                .shadow(10.dp, CircleShape)
                                .clip(CircleShape)
                                .background(VaultTheme.scheme.surface)
                                .clickable(enabled = open, role = Role.Button, interactionSource = null, indication = ripple()) { onSelect(option.action) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(painterResource(option.icon), option.label, tint = option.tint, modifier = Modifier.size(24.dp))
                        }
                        Text(
                            option.label,
                            style = VaultTheme.typography.labelSmall,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                }
            }
        }
    }
}

@ThemePreviews
@Composable
private fun RadialAddMenuPreview() = VaultPreview {
    Box(Modifier.size(360.dp, 420.dp)) {
        RadialAddMenu(
            open = true,
            options = listOf(
                AddOption(AddAction.CAMERA, VaultIcons.Camera, "Camera", VaultTheme.scheme.primary),
                AddOption(AddAction.MUSIC, VaultIcons.Music, "Music", VaultTheme.colors.audioAccent),
                AddOption(AddAction.FILES, VaultIcons.File, "Files", VaultTheme.colors.fileAccent),
                AddOption(AddAction.VIDEOS, VaultIcons.Video, "Videos", VaultTheme.colors.videoAccent),
                AddOption(AddAction.PHOTOS, VaultIcons.Photo, "Photo", VaultTheme.colors.photoAccent),
            ),
            onSelect = {},
            onDismiss = {},
        )
    }
}
