package com.calculator.vault.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.calculator.vault.core.designsystem.R
import com.calculator.vault.core.designsystem.icon.VaultIcons
import com.calculator.vault.core.designsystem.preview.ThemePreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultTheme

/** Every key on the calculator. */
enum class CalcKey(val label: String) {
    Clear("AC"), Negate("+/-"), Percent("%"), Divide("÷"),
    Seven("7"), Eight("8"), Nine("9"), Multiply("×"),
    Four("4"), Five("5"), Six("6"), Subtract("−"),
    One("1"), Two("2"), Three("3"), Add("+"),
    Zero("0"), Decimal("."), Backspace("⌫"), Equals("=");

    val isDigit: Boolean get() = label.length == 1 && label[0].isDigit()
    val digit: Int? get() = label.toIntOrNull()?.takeIf { isDigit }
    val isOperator: Boolean get() = this in setOf(Divide, Multiply, Subtract, Add)
    val isFunction: Boolean get() = this in setOf(Clear, Negate, Percent)

    companion object {
        val rows: List<List<CalcKey>> = entries.chunked(4)
    }
}

/**
 * Five rows × four columns of [KeypadKey]s with the design's colours: white digits, sand
 * function/operator keys, blue "=". Keys fill the available space, so the same composable
 * works in portrait (tall keys) and landscape (wide keys).
 */
@Composable
fun CalculatorKeypad(
    onKey: (CalcKey) -> Unit,
    modifier: Modifier = Modifier,
    spacing: Dp = 6.dp,
    onEqualsLongPress: (() -> Unit)? = null,
    highlightEquals: Boolean = false,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(spacing)) {
        CalcKey.rows.forEach { row ->
            Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing)) {
                row.forEach { key ->
                    val equals = key == CalcKey.Equals
                    val container = when {
                        equals -> VaultTheme.scheme.primary
                        key.isOperator || key.isFunction -> VaultTheme.colors.keyOperator
                        else -> VaultTheme.colors.keyDigit
                    }
                    KeypadKey(
                        onClick = { onKey(key) },
                        onLongClick = if (equals) onEqualsLongPress else null,
                        container = container,
                        contentColor = if (equals) Color.White else VaultTheme.colors.onKey,
                        contentDescription = if (key == CalcKey.Backspace) stringResource(R.string.ds_cd_backspace) else key.label,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .then(if (equals && highlightEquals) Modifier else Modifier),
                    ) {
                        if (key == CalcKey.Backspace) {
                            Icon(painterResource(VaultIcons.Backspace), null, modifier = Modifier.size(24.dp))
                        } else {
                            KeyLabel(key.label)
                        }
                    }
                }
            }
        }
    }
}

@ThemePreviews
@Composable
private fun CalculatorKeypadPreview() = VaultPreview {
    Box(Modifier.size(360.dp, 440.dp).padding(16.dp), contentAlignment = Alignment.Center) {
        CalculatorKeypad(onKey = {}, modifier = Modifier.fillMaxSize())
    }
}
