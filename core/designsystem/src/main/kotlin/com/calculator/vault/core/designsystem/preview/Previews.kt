package com.calculator.vault.core.designsystem.preview

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.calculator.vault.core.designsystem.theme.CalculatorVaultTheme

/** Light + dark previews of a component or dialog. */
@Preview(name = "Light", showBackground = true, backgroundColor = 0xFFF7F6F2)
@Preview(name = "Dark", showBackground = true, backgroundColor = 0xFF0F1117, uiMode = Configuration.UI_MODE_NIGHT_YES)
annotation class ThemePreviews

/** Full screens in light, dark, landscape and tablet. */
@Preview(name = "Phone · Light", device = "spec:width=360dp,height=806dp,dpi=420", showSystemUi = false)
@Preview(name = "Phone · Dark", device = "spec:width=360dp,height=806dp,dpi=420", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Landscape", device = "spec:width=806dp,height=360dp,dpi=420,orientation=landscape")
@Preview(name = "Tablet", device = "spec:width=1280dp,height=800dp,dpi=240")
annotation class ScreenPreviews

/** Wraps preview content in the app theme with the screen background. */
@Composable
fun VaultPreview(content: @Composable () -> Unit) {
    CalculatorVaultTheme {
        Box(Modifier.background(MaterialTheme.colorScheme.background)) { content() }
    }
}
