package com.calculator.vault.feature.photos.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.calculator.vault.core.designsystem.component.VaultDialog
import com.calculator.vault.core.designsystem.component.VaultDialogBody
import com.calculator.vault.core.designsystem.preview.ThemePreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultRadius
import com.calculator.vault.core.designsystem.theme.VaultTheme
import com.calculator.vault.feature.photos.R

private val Intervals = listOf(1, 2, 3, 5, 10)

/** "Slideshow interval" with the selected value highlighted like a wheel picker. */
@Composable
fun IntervalPickerDialog(current: Int, onSave: (Int) -> Unit, onDismiss: () -> Unit) {
    var selected by rememberSaveable { mutableIntStateOf(current) }
    VaultDialog(
        title = stringResource(R.string.photos_slideshow_interval),
        confirmText = stringResource(R.string.photos_save),
        onConfirm = { onSave(selected) },
        onDismissRequest = onDismiss,
    ) { IntervalOptions(selected) { selected = it } }
}

@Composable
private fun IntervalOptions(selected: Int, onSelect: (Int) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Intervals.forEach { seconds ->
            val isSelected = seconds == selected
            val bg by animateColorAsState(if (isSelected) VaultTheme.scheme.primaryContainer else Color.Transparent, label = "intervalBg")
            Box(
                Modifier.fillMaxWidth().height(46.dp).clip(VaultRadius.Tile).background(bg).clickable { onSelect(seconds) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(R.string.photos_seconds, seconds),
                    style = if (isSelected) VaultTheme.typography.headlineMedium else VaultTheme.typography.bodyLarge,
                    color = if (isSelected) VaultTheme.scheme.onSurface else VaultTheme.scheme.onSurfaceVariant,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                )
            }
        }
    }
}

@ThemePreviews
@Composable
private fun IntervalPickerPreview() = VaultPreview {
    Column(Modifier.padding(16.dp).background(VaultTheme.scheme.surface, VaultRadius.Dialog).padding(20.dp)) {
        VaultDialogBody(stringResource(R.string.photos_slideshow_interval), "Save", {}, dismissText = "Cancel") {
            IntervalOptions(2) {}
        }
    }
}
