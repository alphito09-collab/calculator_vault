# :feature:language

**Language picker.** Lists the app's UI languages and applies the choice as a per-app locale. It runs during first-run setup and from Settings.

| | |
|---|---|
| Gradle path | `:feature:language` |
| Namespace | `com.calculator.vault.feature.language` |
| Plugins | `calculatorvault.android.feature` |
| Routes | `LanguageRoute(fromSettings)` |
| Koin module | `com.calculator.vault.feature.language.di.LanguageModule` |
| Navigation | `EntryProviderScope<NavKey>.languageEntries(navigator)` in `navigation/` |

## Languages

System default, English, Chinese, German, Arabic, Spanish, French, Hindi, Indonesian, Portuguese,
Russian, Turkish and Urdu. `:app` limits packaged resources to the same set with `localeFilters`.
Only English strings exist so far; the other translations are still to be added.

## Structure

| Package | Classes |
|---|---|
| `domain/model` | `AppLanguage(tag, englishName, nativeName, flag)`. A `null` tag means "follow the system" |
| `domain/repository` | `LanguageRepository` |
| `data` | `LanguageRepositoryImpl`: `AppCompatDelegate.setApplicationLocales(...)` (this recreates activities) and persists the choice |
| `domain/usecase` | `GetLanguagesUseCase`, `ApplyLanguageUseCase` |
| `presentation/mvi` | `LanguageContract`, `LanguageViewModel` |
| `presentation/ui` | `LanguageScreen` |
| `presentation/components` | `LanguageOption`: flag or globe, English and native name, radio button, blue outline when selected |

## Dependencies

Standard feature dependencies come from `calculatorvault.android.feature`: `:core:common`, `:core:model`, `:core:designsystem`, `:core:ui`, `:core:navigation`, `:core:domain`, Lifecycle Compose, Navigation 3, kotlinx.serialization and Koin Compose.

Additional dependencies: AppCompat (per-app locales).
