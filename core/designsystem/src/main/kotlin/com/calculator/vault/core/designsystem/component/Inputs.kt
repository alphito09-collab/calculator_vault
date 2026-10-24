package com.calculator.vault.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.calculator.vault.core.designsystem.preview.ThemePreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultRadius
import com.calculator.vault.core.designsystem.theme.VaultTheme

/** White 64dp rounded input with a grey placeholder ("Enter you email address"). */
@Composable
fun VaultTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    isError: Boolean = false,
    errorText: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    Column(modifier) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            textStyle = VaultTheme.text.rowTitle.copy(color = VaultTheme.scheme.onSurface),
            cursorBrush = SolidColor(VaultTheme.scheme.primary),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .clip(VaultRadius.Card)
                .background(VaultTheme.scheme.surface)
                .then(if (isError) Modifier.border(1.dp, VaultTheme.scheme.error, VaultRadius.Card) else Modifier),
            decorationBox = { inner ->
                Box(Modifier.padding(horizontal = 20.dp, vertical = 20.dp), contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text(placeholder, style = VaultTheme.text.rowTitle, color = VaultTheme.scheme.onSurfaceVariant.copy(alpha = 0.8f))
                    }
                    inner()
                }
            },
        )
        if (isError && errorText != null) {
            Text(
                errorText,
                style = VaultTheme.typography.bodySmall,
                color = VaultTheme.scheme.error,
                modifier = Modifier.padding(start = 20.dp, top = 6.dp),
            )
        }
    }
}

/** Underlined single-line field for dialogs ("Rename file", "Previous 4-digit PIN"). */
@Composable
fun UnderlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    isError: Boolean = false,
    errorText: String? = null,
) {
    Column(modifier) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = VaultTheme.text.rowTitle.copy(color = VaultTheme.scheme.onSurface),
            cursorBrush = SolidColor(VaultTheme.scheme.primary),
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            decorationBox = { inner ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty() && placeholder != null) {
                        Text(placeholder, style = VaultTheme.text.rowTitle, color = VaultTheme.scheme.onSurfaceVariant.copy(alpha = 0.8f))
                    }
                    inner()
                }
            },
        )
        HorizontalDivider(thickness = 1.dp, color = if (isError) VaultTheme.scheme.error else VaultTheme.scheme.onSurface)
        if (isError && errorText != null) {
            Text(errorText, style = VaultTheme.typography.bodySmall, color = VaultTheme.scheme.error, modifier = Modifier.padding(top = 6.dp))
        }
    }
}

/** Horizontally scrolling filter chips ("All  Photos  Videos …"). */
@Composable
fun <T> FilterChipRow(
    options: List<T>,
    selected: T,
    label: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp),
) {
    Row(
        modifier.horizontalScroll(rememberScrollState()).padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            Text(
                text = label(option),
                style = VaultTheme.typography.labelMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Normal, fontSize = VaultTheme.text.rowTitle.fontSize),
                color = if (isSelected) VaultTheme.scheme.secondary else VaultTheme.scheme.onSurfaceVariant,
                modifier = Modifier
                    .clip(VaultRadius.Pill)
                    .background(if (isSelected) VaultTheme.scheme.primaryContainer else VaultTheme.colors.iconTile)
                    .then(if (isSelected) Modifier.border(1.dp, VaultTheme.scheme.primary.copy(alpha = 0.6f), VaultRadius.Pill) else Modifier)
                    .clickable(role = Role.Tab) { onSelect(option) }
                    .padding(horizontal = 20.dp, vertical = 10.dp),
            )
        }
    }
}

@Composable
fun VaultSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    enabled: Boolean = true,
    onValueChangeFinished: (() -> Unit)? = null,
) {
    Slider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        valueRange = valueRange,
        enabled = enabled,
        onValueChangeFinished = onValueChangeFinished,
        colors = SliderDefaults.colors(
            thumbColor = VaultTheme.scheme.primary,
            activeTrackColor = VaultTheme.scheme.primary,
            inactiveTrackColor = VaultTheme.colors.cardBorder,
            activeTickColor = androidx.compose.ui.graphics.Color.Transparent,
            inactiveTickColor = androidx.compose.ui.graphics.Color.Transparent,
        ),
    )
}

@ThemePreviews
@Composable
private fun InputsPreview() = VaultPreview {
    Column(Modifier.padding(vertical = 16.dp)) {
        VaultTextField("", {}, "Enter you email address", Modifier.padding(horizontal = 20.dp))
        VaultTextField("example@gmail.com", {}, "", Modifier.padding(20.dp))
        FilterChipRow(listOf("All", "Photos", "Videos", "Audio", "Files"), "All", { it }, {})
        VaultSlider(0.5f, {}, Modifier.padding(20.dp))
        UnderlinedTextField("IMG_20250702_100123", {}, Modifier.padding(horizontal = 20.dp))
        Box(Modifier.clickable { })
    }
}
