package com.calculator.vault.feature.recovery.presentation.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.calculator.vault.core.designsystem.component.SelectionIndicator
import com.calculator.vault.core.designsystem.component.SelectionIndicatorStyle
import com.calculator.vault.core.designsystem.component.VaultCard
import com.calculator.vault.core.designsystem.component.VaultSwitch
import com.calculator.vault.core.designsystem.component.cardBorder
import com.calculator.vault.core.designsystem.icon.VaultIcons
import com.calculator.vault.core.designsystem.preview.ThemePreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultRadius
import com.calculator.vault.core.designsystem.theme.VaultTheme
import com.calculator.vault.core.model.SecurityQuestion
import com.calculator.vault.feature.recovery.R

sealed interface OptionTrailing {
    data class Toggle(val checked: Boolean, val onChange: (Boolean) -> Unit) : OptionTrailing
    data class Chevron(val configured: Boolean) : OptionTrailing
}

/** Recovery option card: navy icon in a grey pill, label, toggle or chevron (+ ✓ when set). */
@Composable
fun RecoveryOptionCard(
    @DrawableRes icon: Int,
    title: String,
    trailing: OptionTrailing,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
) {
    VaultCard(
        modifier = modifier.fillMaxWidth(),
        border = cardBorder(),
        onClick = onClick ?: (trailing as? OptionTrailing.Toggle)?.let { t -> { t.onChange(!t.checked) } },
    ) {
        Row(Modifier.heightIn(min = 68.dp).padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(width = 45.dp, height = 36.dp).clip(VaultRadius.Pill).background(VaultTheme.colors.iconTile),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painterResource(icon), null, tint = VaultTheme.scheme.secondary, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = VaultTheme.text.rowTitle.copy(fontSize = VaultTheme.typography.bodyMedium.fontSize), color = VaultTheme.scheme.onSurface)
                if (subtitle != null) Text(subtitle, style = VaultTheme.typography.bodySmall, color = VaultTheme.scheme.primary)
            }
            when (trailing) {
                is OptionTrailing.Toggle -> VaultSwitch(trailing.checked, trailing.onChange)
                is OptionTrailing.Chevron -> Row(verticalAlignment = Alignment.CenterVertically) {
                    if (trailing.configured) {
                        SelectionIndicator(true, size = 20.dp, style = SelectionIndicatorStyle.OnSurface)
                        Spacer(Modifier.width(8.dp))
                    }
                    Icon(painterResource(VaultIcons.ChevronRight), null, tint = VaultTheme.colors.iconTileContent, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@StringRes
fun SecurityQuestion.textRes(): Int = when (this) {
    SecurityQuestion.FIRST_PET -> R.string.recovery_q_first_pet
    SecurityQuestion.BIRTH_CITY -> R.string.recovery_q_birth_city
    SecurityQuestion.FIRST_SCHOOL -> R.string.recovery_q_first_school
    SecurityQuestion.CHILDHOOD_FRIEND -> R.string.recovery_q_childhood_friend
    SecurityQuestion.FAVORITE_TEACHER -> R.string.recovery_q_favorite_teacher
    SecurityQuestion.MOTHER_MAIDEN_NAME -> R.string.recovery_q_mother_maiden
}

/** Six digit boxes backed by one hidden text field (works with SMS/clipboard autofill). */
@Composable
fun OtpInput(code: String, onCodeChange: (String) -> Unit, length: Int, isError: Boolean, modifier: Modifier = Modifier) {
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }
    BasicTextField(
        value = code,
        onValueChange = onCodeChange,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        modifier = modifier.focusRequester(focus),
        decorationBox = {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.widthIn(max = 420.dp)) {
                repeat(length) { index ->
                    val char = code.getOrNull(index)?.toString() ?: ""
                    val active = index == code.length
                    val border by animateColorAsState(
                        when {
                            isError -> VaultTheme.scheme.error
                            active -> VaultTheme.scheme.primary
                            else -> VaultTheme.colors.cardBorder
                        },
                        label = "otpBorder",
                    )
                    Box(
                        Modifier
                            .weight(1f)
                            .aspectRatio(0.85f)
                            .clip(VaultTheme.shapes.medium)
                            .background(VaultTheme.scheme.surface)
                            .border(BorderStroke(if (active) 2.dp else 1.dp, border), VaultTheme.shapes.medium),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(char, style = VaultTheme.typography.headlineMedium, color = VaultTheme.scheme.onSurface, textAlign = TextAlign.Center)
                    }
                }
            }
        },
    )
}

@ThemePreviews
@Composable
private fun RecoveryComponentsPreview() = VaultPreview {
    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        RecoveryOptionCard(VaultIcons.FingerprintLine, stringResource(R.string.recovery_add_fingerprint), OptionTrailing.Toggle(true) {})
        RecoveryOptionCard(VaultIcons.PatternSmall, stringResource(R.string.recovery_add_pattern), OptionTrailing.Chevron(true), onClick = {})
        RecoveryOptionCard(VaultIcons.Mail, stringResource(R.string.recovery_add_email), OptionTrailing.Chevron(false), subtitle = "me@mail.com", onClick = {})
        OtpInput("123", {}, 6, isError = false, modifier = Modifier.fillMaxWidth())
    }
}
