package com.calculator.vault.feature.language.domain.usecase

import com.calculator.vault.feature.language.domain.model.AppLanguage
import com.calculator.vault.feature.language.domain.repository.LanguageRepository
import org.koin.core.annotation.Factory

@Factory
class GetLanguagesUseCase(private val repository: LanguageRepository) {
    operator fun invoke(): Pair<List<AppLanguage>, String?> = repository.supportedLanguages() to repository.currentTag()
}

@Factory
class ApplyLanguageUseCase(private val repository: LanguageRepository) {
    suspend operator fun invoke(tag: String?) = repository.apply(tag)
}
