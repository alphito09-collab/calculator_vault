package com.calculator.vault.feature.language.domain.repository

import com.calculator.vault.feature.language.domain.model.AppLanguage

interface LanguageRepository {
    fun supportedLanguages(): List<AppLanguage>
    fun currentTag(): String?

    /** Applies the per-app locale (recreates activities) and persists the choice. */
    suspend fun apply(tag: String?)
}
