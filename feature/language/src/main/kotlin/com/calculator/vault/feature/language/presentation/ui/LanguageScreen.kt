package com.calculator.vault.feature.language.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calculator.vault.core.designsystem.component.NavigationIcon
import com.calculator.vault.core.designsystem.component.VaultButton
import com.calculator.vault.core.designsystem.component.VaultTopBar
import com.calculator.vault.core.designsystem.preview.ScreenPreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.ui.component.VaultScreen
import com.calculator.vault.core.ui.mvi.CollectEffects
import com.calculator.vault.feature.language.R
import com.calculator.vault.feature.language.domain.model.AppLanguage
import com.calculator.vault.feature.language.presentation.components.LanguageOption
import com.calculator.vault.feature.language.presentation.mvi.LanguageEffect
import com.calculator.vault.feature.language.presentation.mvi.LanguageIntent
import com.calculator.vault.feature.language.presentation.mvi.LanguageState
import com.calculator.vault.feature.language.presentation.mvi.LanguageViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun LanguageRoute(
    fromSettings: Boolean,
    onContinue: () -> Unit,
    onClose: () -> Unit,
    viewModel: LanguageViewModel = koinViewModel(key = "language-$fromSettings") { parametersOf(fromSettings) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    viewModel.effects.CollectEffects { effect ->
        when (effect) {
            LanguageEffect.Continue -> onContinue()
            LanguageEffect.Close -> onClose()
        }
    }
    LanguageScreen(state, viewModel::onIntent)
}

@Composable
fun LanguageScreen(state: LanguageState, onIntent: (LanguageIntent) -> Unit) {
    VaultScreen(
        topBar = {
            VaultTopBar(
                title = stringResource(R.string.language_title),
                navigationIcon = if (state.fromSettings) NavigationIcon.Back else null,
                onNavigationClick = { onIntent(LanguageIntent.Back) },
            )
        },
        bottomBar = {
            VaultButton(
                text = stringResource(R.string.language_save),
                onClick = { onIntent(LanguageIntent.Save) },
                loading = state.saving,
            )
        },
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 300.dp),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(state.languages, key = { it.tag ?: "system" }) { language ->
                LanguageOption(
                    language = language,
                    selected = language.tag == state.selectedTag,
                    systemLabel = stringResource(R.string.language_system),
                    onClick = { onIntent(LanguageIntent.Select(language.tag)) },
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}

@ScreenPreviews
@Composable
private fun LanguagePreview() = VaultPreview {
    LanguageScreen(
        LanguageState(
            languages = listOf(
                AppLanguage(null, "System", "System", ""),
                AppLanguage("zh", "Chinese", "中文", "🇨🇳"),
                AppLanguage("de", "German", "Deutsch", "🇩🇪"),
                AppLanguage("ar", "Arabic", "العربية", "🇸🇦"),
            ),
            selectedTag = null,
        ),
        onIntent = {},
    )
}
