package com.calculator.vault.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.calculator.vault.core.designsystem.theme.VaultRadius
import com.calculator.vault.core.designsystem.theme.VaultTheme

/** Overflow menu in the design's style: 24dp radius, white, roomy rows. */
@Composable
fun VaultDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset(0.dp, 0.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier.widthIn(min = 180.dp),
        offset = offset,
        shape = VaultRadius.Card,
        containerColor = VaultTheme.scheme.surface,
        shadowElevation = 12.dp,
        content = { Box(Modifier.padding(vertical = 8.dp)) { androidx.compose.foundation.layout.Column(content = content) } },
    )
}

@Composable
fun VaultMenuItem(text: String, onClick: () -> Unit, enabled: Boolean = true, destructive: Boolean = false) {
    DropdownMenuItem(
        text = {
            Text(
                text,
                style = VaultTheme.text.rowTitle,
                color = if (destructive) VaultTheme.scheme.error else VaultTheme.scheme.onSurface,
            )
        },
        onClick = onClick,
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 4.dp),
        colors = MenuDefaults.itemColors(),
    )
}
