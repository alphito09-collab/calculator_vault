package com.calculator.vault.feature.language.presentation.mvi

import com.calculator.vault.core.ui.mvi.UiEffect
import com.calculator.vault.core.ui.mvi.UiIntent
import com.calculator.vault.core.ui.mvi.UiState
import com.calculator.vault.feature.language.domain.model.AppLanguage

data class LanguageState(
    val languages: List<AppLanguage> = emptyList(),
    val selectedTag: String? = null,
    val fromSettings: Boolean = false,
    val saving: Boolean = false,
) : UiState

sealed interface LanguageIntent : UiIntent {
    data class Select(val tag: String?) : LanguageIntent
    data object Save : LanguageIntent
    data object Back : LanguageIntent
}

sealed interface LanguageEffect : UiEffect {
    data object Continue : LanguageEffect
    data object Close : LanguageEffect
}
