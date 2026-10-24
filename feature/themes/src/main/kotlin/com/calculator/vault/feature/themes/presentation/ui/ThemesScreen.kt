package com.calculator.vault.feature.themes.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calculator.vault.core.designsystem.component.VaultTopBar
import com.calculator.vault.core.designsystem.preview.ScreenPreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultTheme
import com.calculator.vault.core.model.ThemeMode
import com.calculator.vault.core.ui.component.VaultScreen
import com.calculator.vault.core.ui.mvi.CollectEffects
import com.calculator.vault.feature.themes.R
import com.calculator.vault.feature.themes.presentation.components.ThemeOptionCard
import com.calculator.vault.feature.themes.presentation.mvi.ThemesEffect
import com.calculator.vault.feature.themes.presentation.mvi.ThemesIntent
import com.calculator.vault.feature.themes.presentation.mvi.ThemesState
import com.calculator.vault.feature.themes.presentation.mvi.ThemesViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun ThemesRoute(onClose: () -> Unit, viewModel: ThemesViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    viewModel.effects.CollectEffects { if (it == ThemesEffect.Close) onClose() }
    ThemesScreen(state, viewModel::onIntent)
}

@Composable
fun ThemesScreen(state: ThemesState, onIntent: (ThemesIntent) -> Unit) {
    VaultScreen(topBar = { VaultTopBar(stringResource(R.string.themes_title), onNavigationClick = { onIntent(ThemesIntent.Back) }) }) {
        Column(
            Modifier.align(Alignment.TopCenter).widthIn(max = 560.dp).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.themes_subtitle), style = VaultTheme.text.rowTitle, color = VaultTheme.scheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
            listOf(
                Triple(ThemeMode.SYSTEM, R.string.themes_system, R.string.themes_system_desc),
                Triple(ThemeMode.LIGHT, R.string.themes_light, R.string.themes_light_desc),
                Triple(ThemeMode.DARK, R.string.themes_dark, R.string.themes_dark_desc),
            ).forEach { (mode, title, desc) ->
                ThemeOptionCard(mode, stringResource(title), stringResource(desc), state.mode == mode, { onIntent(ThemesIntent.Select(mode)) })
            }
        }
    }
}

@ScreenPreviews
@Composable
private fun ThemesPreview() = VaultPreview { ThemesScreen(ThemesState(ThemeMode.LIGHT), onIntent = {}) }
