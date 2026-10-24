package com.calculator.vault.feature.calculator.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calculator.vault.core.designsystem.preview.ThemePreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultTheme

/** Expression line plus a large result that shrinks to fit long numbers. */
@Composable
fun CalculatorDisplay(subtitle: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        AnimatedContent(
            targetState = subtitle,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "calcSubtitle",
        ) { text ->
            Text(
                text,
                style = VaultTheme.typography.headlineSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Normal),
                color = VaultTheme.scheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.StartEllipsis,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        AnimatedContent(
            targetState = value,
            transitionSpec = { (slideInVertically { it / 4 } + fadeIn()) togetherWith (slideOutVertically { -it / 4 } + fadeOut()) },
            label = "calcValue",
        ) { text ->
            BasicText(
                text = text,
                style = VaultTheme.typography.displayLarge.copy(color = VaultTheme.scheme.onBackground, textAlign = TextAlign.End),
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(minFontSize = 28.sp, maxFontSize = 64.sp, stepSize = 2.sp),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@ThemePreviews
@Composable
private fun CalculatorDisplayPreview() = VaultPreview {
    CalculatorDisplay("12 + 7 ×", "1,234,567.89", Modifier.padding(24.dp))
}
