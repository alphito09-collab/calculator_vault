package com.calculator.vault.feature.calculator.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.calculator.vault.core.designsystem.component.VaultCard
import com.calculator.vault.core.designsystem.component.VaultDialogContainer
import com.calculator.vault.core.designsystem.component.VaultTextButton
import com.calculator.vault.core.designsystem.component.cardBorder
import com.calculator.vault.core.designsystem.icon.VaultIcons
import com.calculator.vault.core.designsystem.preview.ThemePreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultRadius
import com.calculator.vault.core.designsystem.theme.VaultTheme
import com.calculator.vault.core.model.RecoveryMethod
import com.calculator.vault.core.ui.component.actionRes
import com.calculator.vault.core.ui.component.iconRes
import com.calculator.vault.feature.calculator.R

/**
 * Offered on the calculator after too many wrong PINs. One recovery method is shown with a
 * "Continue" action; several are listed so the user can pick one.
 */
@Composable
fun ForgotPinDialog(
    methods: List<RecoveryMethod>,
    onChoose: (RecoveryMethod) -> Unit,
    onDismiss: () -> Unit,
) {
    VaultDialogContainer(onDismissRequest = onDismiss) {
        ForgotPinDialogBody(methods, onChoose, onDismiss)
    }
}

/** The dialog's content; exposed so previews can render it without a window. Scrolls in landscape. */
@Composable
fun ColumnScope.ForgotPinDialogBody(
    methods: List<RecoveryMethod>,
    onChoose: (RecoveryMethod) -> Unit,
    onDismiss: () -> Unit,
) {
    val single = methods.singleOrNull()
    Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(stringResource(R.string.calculator_forgot_title), style = VaultTheme.text.dialogTitle, color = VaultTheme.colors.dialogTitle)
        Text(
            stringResource(if (single != null) R.string.calculator_forgot_message_one else R.string.calculator_forgot_message_many),
            style = VaultTheme.text.rowTitle,
            color = VaultTheme.scheme.onSurfaceVariant,
        )
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            methods.forEach { method ->
                MethodRow(method, onClick = if (single == null) ({ onChoose(method) }) else null)
            }
        }
    }
    Spacer(Modifier.height(24.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
        VaultTextButton(stringResource(R.string.calculator_cancel), onDismiss)
        if (single != null) VaultTextButton(stringResource(R.string.calculator_forgot_continue), { onChoose(single) })
    }
}

/** Icon pill + "Draw your pattern"; tappable (with a chevron) when there is a choice to make. */
@Composable
private fun MethodRow(method: RecoveryMethod, onClick: (() -> Unit)?) {
    VaultCard(Modifier.fillMaxWidth(), onClick = onClick, border = cardBorder()) {
        Row(Modifier.heightIn(min = 60.dp).padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(width = 42.dp, height = 34.dp).clip(VaultRadius.Pill).background(VaultTheme.colors.iconTile),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painterResource(method.iconRes()), null, tint = VaultTheme.scheme.secondary, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text(stringResource(method.actionRes()), style = VaultTheme.text.rowTitle, color = VaultTheme.scheme.onSurface, modifier = Modifier.weight(1f))
            if (onClick != null) {
                Icon(painterResource(VaultIcons.ChevronRight), null, tint = VaultTheme.colors.iconTileContent, modifier = Modifier.size(15.dp))
            }
        }
    }
}

@ThemePreviews
@Composable
private fun ForgotPinDialogManyPreview() = DialogPreviewFrame {
    ForgotPinDialogBody(listOf(RecoveryMethod.FINGERPRINT, RecoveryMethod.PATTERN, RecoveryMethod.SECURITY_QUESTION), {}, {})
}

@ThemePreviews
@Composable
private fun ForgotPinDialogOnePreview() = DialogPreviewFrame {
    ForgotPinDialogBody(listOf(RecoveryMethod.PATTERN), {}, {})
}

@Composable
private fun DialogPreviewFrame(content: @Composable ColumnScope.() -> Unit) = VaultPreview {
    Box(Modifier.background(Color.Black.copy(alpha = 0.35f)).padding(16.dp)) {
        Column(
            Modifier.clip(VaultRadius.Dialog).background(VaultTheme.scheme.surface).padding(horizontal = 20.dp, vertical = 24.dp),
            content = content,
        )
    }
}
