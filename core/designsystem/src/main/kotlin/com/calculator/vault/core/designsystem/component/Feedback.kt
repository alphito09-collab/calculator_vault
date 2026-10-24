package com.calculator.vault.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.calculator.vault.core.designsystem.icon.VaultIcons
import com.calculator.vault.core.designsystem.preview.ThemePreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultRadius
import com.calculator.vault.core.designsystem.theme.VaultTheme

enum class ToastKind { Success, Info, Error }

/** Snackbar payload carrying a [ToastKind] so the host can pick the leading icon. */
data class VaultToastVisuals(
    override val message: String,
    val kind: ToastKind = ToastKind.Success,
    override val actionLabel: String? = null,
    override val withDismissAction: Boolean = false,
    override val duration: SnackbarDuration = if (actionLabel == null) SnackbarDuration.Short else SnackbarDuration.Long,
) : SnackbarVisuals

suspend fun SnackbarHostState.showToast(message: String, kind: ToastKind = ToastKind.Success) =
    showSnackbar(VaultToastVisuals(message, kind))

/** Black pill toast ("✓ App locked") rendered for every snackbar. */
@Composable
fun VaultSnackbarHost(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(hostState, modifier) { data -> VaultToast(data) }
}

@Composable
private fun VaultToast(data: SnackbarData) {
    val visuals = data.visuals
    val kind = (visuals as? VaultToastVisuals)?.kind ?: ToastKind.Info
    VaultToastContent(visuals.message, kind, visuals.actionLabel, onAction = { data.performAction() })
}

@Composable
fun VaultToastContent(message: String, kind: ToastKind, actionLabel: String? = null, onAction: () -> Unit = {}) {
    Row(
        Modifier
            .padding(16.dp)
            .shadow(8.dp, VaultRadius.Pill)
            .clip(VaultRadius.Pill)
            .background(VaultTheme.colors.toast)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val badge = when (kind) {
            ToastKind.Success -> VaultTheme.scheme.primary
            ToastKind.Info -> VaultTheme.scheme.primary
            ToastKind.Error -> VaultTheme.scheme.error
        }
        Box(Modifier.size(24.dp).clip(CircleShape).background(badge), contentAlignment = Alignment.Center) {
            Icon(
                painterResource(
                    when (kind) {
                        ToastKind.Success -> VaultIcons.Check
                        ToastKind.Info -> VaultIcons.Info
                        ToastKind.Error -> VaultIcons.Warning
                    },
                ),
                null,
                tint = Color.White,
                modifier = Modifier.size(15.dp),
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(message, style = VaultTheme.typography.labelMedium, color = VaultTheme.colors.onToast)
        if (actionLabel != null) {
            Spacer(Modifier.width(8.dp))
            VaultTextButton(actionLabel, onAction, small = true)
        }
    }
}

/** Black speech bubble with a downward arrow ("Now press equal"). */
@Composable
fun TooltipBubble(text: String, modifier: Modifier = Modifier, arrowOffsetFraction: Float = 0.75f) {
    Column(modifier, horizontalAlignment = Alignment.Start) {
        Text(
            text,
            style = VaultTheme.text.rowTitle,
            color = VaultTheme.colors.onToast,
            modifier = Modifier
                .clip(VaultTheme.shapes.extraSmall)
                .background(VaultTheme.colors.toast)
                .padding(10.dp),
        )
        Box(
            Modifier
                .padding(start = 0.dp)
                .align(Alignment.End)
                .padding(end = 24.dp)
                .size(width = 14.dp, height = 8.dp)
                .background(VaultTheme.colors.toast, TriangleDown),
        )
    }
}

private val TriangleDown = GenericShape { size, _ ->
    moveTo(0f, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width / 2f, size.height)
    close()
}

@ThemePreviews
@Composable
private fun FeedbackPreview() = VaultPreview {
    Column(Modifier.padding(16.dp)) {
        VaultToastContent("App locked", ToastKind.Success)
        VaultToastContent("No internet connection", ToastKind.Error)
        TooltipBubble("Now press equal")
    }
}
