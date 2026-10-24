# :feature:themes

**Theme picker.** Lets the user choose **System default**, **Light** or **Dark**.

| | |
|---|---|
| Gradle path | `:feature:themes` |
| Namespace | `com.calculator.vault.feature.themes` |
| Plugins | `calculatorvault.android.feature` |
| Routes | `ThemesRoute` |
| Koin module | `com.calculator.vault.feature.themes.di.ThemesModule` |
| Navigation | `EntryProviderScope<NavKey>.themesEntries(navigator)` in `navigation/` |

## Structure

| Package | Classes |
|---|---|
| `domain/usecase` | `ObserveThemeModeUseCase`, `SetThemeModeUseCase` |
| `presentation/mvi` | `ThemesMvi` (`ThemesState(mode)`, `Select`, `Back`, `ThemesViewModel`) |
| `presentation/ui` | `ThemesScreen` |
| `presentation/components` | `ThemeOptionCard`: a small screen mock in the target theme's colours, split in two for "System default" |

The selected `ThemeMode` is stored in `UserSettings` and applied by `VaultApp` in `:app`.

## Dependencies

Standard feature dependencies come from `calculatorvault.android.feature`: `:core:common`, `:core:model`, `:core:designsystem`, `:core:ui`, `:core:navigation`, `:core:domain`, Lifecycle Compose, Navigation 3, kotlinx.serialization and Koin Compose.
