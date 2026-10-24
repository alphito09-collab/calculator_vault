package com.calculator.vault.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.calculator.vault.core.designsystem.preview.ScreenPreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultTheme
import com.calculator.vault.core.model.AppIconOption

/**
 * Opaque stand-in for the vault while the window is visible to someone else's UI (the Recents
 * live tile, the notification shade). Looks like the splash so the disguise holds.
 */
@Composable
fun PrivacyCover(icon: AppIconOption, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().background(VaultTheme.scheme.background), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            AppIconArt(icon = icon, size = 104.dp)
            Spacer(Modifier.height(20.dp))
            Text(stringResource(icon.labelRes()), style = VaultTheme.typography.headlineSmall, color = VaultTheme.scheme.onBackground)
        }
    }
}

@ScreenPreviews
@Composable
private fun PrivacyCoverPreview() = VaultPreview { PrivacyCover(AppIconOption.DEFAULT) }
