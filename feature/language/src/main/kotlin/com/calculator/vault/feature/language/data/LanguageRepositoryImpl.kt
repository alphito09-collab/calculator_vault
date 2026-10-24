package com.calculator.vault.feature.language.data

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.calculator.vault.core.domain.repository.SettingsRepository
import com.calculator.vault.feature.language.domain.model.AppLanguage
import com.calculator.vault.feature.language.domain.repository.LanguageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single

@Single(binds = [LanguageRepository::class])
class LanguageRepositoryImpl(private val settings: SettingsRepository) : LanguageRepository {

    override fun supportedLanguages(): List<AppLanguage> = LANGUAGES

    override fun currentTag(): String? =
        AppCompatDelegate.getApplicationLocales().takeIf { !it.isEmpty }?.get(0)?.language

    override suspend fun apply(tag: String?) {
        settings.setLanguage(tag)
        withContext(Dispatchers.Main) {
            AppCompatDelegate.setApplicationLocales(
                if (tag == null) LocaleListCompat.getEmptyLocaleList() else LocaleListCompat.forLanguageTags(tag),
            )
        }
    }

    private companion object {
        val LANGUAGES = listOf(
            AppLanguage(null, "System", "System", ""),
            AppLanguage("en", "English", "English", "🇺🇸"),
            AppLanguage("zh", "Chinese", "中文", "🇨🇳"),
            AppLanguage("de", "German", "Deutsch", "🇩🇪"),
            AppLanguage("ar", "Arabic", "العربية", "🇸🇦"),
            AppLanguage("es", "Spanish", "Español", "🇪🇸"),
            AppLanguage("fr", "French", "Français", "🇫🇷"),
            AppLanguage("hi", "Hindi", "हिन्दी", "🇮🇳"),
            AppLanguage("in", "Indonesian", "Bahasa Indonesia", "🇮🇩"),
            AppLanguage("pt", "Portuguese", "Português", "🇧🇷"),
            AppLanguage("ru", "Russian", "Русский", "🇷🇺"),
            AppLanguage("tr", "Turkish", "Türkçe", "🇹🇷"),
            AppLanguage("ur", "Urdu", "اردو", "🇵🇰"),
        )
    }
}
