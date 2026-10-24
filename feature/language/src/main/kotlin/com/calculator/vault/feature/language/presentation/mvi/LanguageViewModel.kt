package com.calculator.vault.feature.language.presentation.mvi

import androidx.lifecycle.viewModelScope
import com.calculator.vault.core.ui.mvi.MviViewModel
import com.calculator.vault.feature.language.domain.usecase.ApplyLanguageUseCase
import com.calculator.vault.feature.language.domain.usecase.GetLanguagesUseCase
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam

@KoinViewModel
class LanguageViewModel(
    @InjectedParam fromSettings: Boolean,
    getLanguages: GetLanguagesUseCase,
    private val applyLanguage: ApplyLanguageUseCase,
) : MviViewModel<LanguageState, LanguageIntent, LanguageEffect>(LanguageState(fromSettings = fromSettings)) {

    init {
        val (languages, current) = getLanguages()
        setState { copy(languages = languages, selectedTag = current) }
    }

    override fun handleIntent(intent: LanguageIntent) {
        when (intent) {
            is LanguageIntent.Select -> setState { copy(selectedTag = intent.tag) }
            LanguageIntent.Save -> save()
            LanguageIntent.Back -> sendEffect(LanguageEffect.Close)
        }
    }

    private fun save() {
        if (currentState.saving) return
        setState { copy(saving = true) }
        viewModelScope.launch {
            applyLanguage(currentState.selectedTag)
            sendEffect(if (currentState.fromSettings) LanguageEffect.Close else LanguageEffect.Continue)
            setState { copy(saving = false) }
        }
    }
}
