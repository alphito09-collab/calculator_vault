# :feature:home

**Vault home and shell.** The first screen inside the vault. It shows the Home and Settings tabs, the floating bottom bar and the radial **+** menu.

| | |
|---|---|
| Gradle path | `:feature:home` |
| Namespace | `com.calculator.vault.feature.home` |
| Plugins | `calculatorvault.android.feature` |
| Routes | `MainRoute(tab = HOME | SETTINGS)` |
| Koin module | `com.calculator.vault.feature.home.di.HomeModule` |
| Navigation | `EntryProviderScope<NavKey>.homeEntries(navigator)` in `navigation/` |

## Screens

- **`MainScreen`**: the vault shell with the Home and Settings tabs and the floating bottom bar.
  The Settings tab content is supplied by `:feature:settings` through `:app`, so this module
  doesn't depend on it.
- **`HomeContent`**: title, a promo banner, **Tools** (App Lock, Themes, Cloud Backup, Intruder)
  and **Files** cards (Photos, Videos, Audios, Files) with item counts.
- **`RadialAddMenu`**: the **+** turns into **×** and fans the options out in a semicircle (Camera,
  Music, Files, Videos, Photos), with spring and stagger animations.
- **`RestoreVaultDialog`**: shown when shared storage holds an encrypted vault from a previous
  install. The user unlocks it with the old PIN, or starts fresh, which archives the old vault.
- Asks for **All files access** when needed (at most once per visit unless the user tries to add
  something) and resumes the pending import once access is granted.
- Back on Home shows an exit confirmation.

## Structure

| Package | Classes |
|---|---|
| `domain/usecase` | `ObserveHomeSummaryUseCase` (counts + `HomeBanner`: add recovery → enable backup → reset-code reminder, in turn until dismissed), `DismissBannerUseCase`, `VaultStorageUseCase` |
| `presentation/mvi` | `HomeContract` (`HomeTool`, `AddAction`), `HomeViewModel` |
| `presentation/ui` | `MainScreen`, `HomeContent` |
| `presentation/components` | `HomeTiles`, `RadialAddMenu`, `RestoreVaultDialog` |

## Dependencies

Standard feature dependencies come from `calculatorvault.android.feature`: `:core:common`, `:core:model`, `:core:designsystem`, `:core:ui`, `:core:navigation`, `:core:domain`, Lifecycle Compose, Navigation 3, kotlinx.serialization and Koin Compose.
