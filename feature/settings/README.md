# :feature:settings

**Settings.** The Settings tab of the vault shell, plus the app-icon disguise and uninstall protection.

| | |
|---|---|
| Gradle path | `:feature:settings` |
| Namespace | `com.calculator.vault.feature.settings` |
| Plugins | `calculatorvault.android.feature` |
| Routes | `MainRoute(tab = SETTINGS)` (via `SettingsTabContent`), `ChangeAppIconRoute` |
| Koin module | `com.calculator.vault.feature.settings.di.SettingsModule` |
| Navigation | `EntryProviderScope<NavKey>.settingsEntries(navigator)` in `navigation/` |

## Settings tab (`SettingsContent`)

| Section | Rows |
|---|---|
| Account | Google login (cloud backup account) |
| Appearance | Theme, App icon, Language |
| Authentication | App Lock, Change PIN, App Lock security, Fingerprint unlock, Recovery methods |
| Privacy | Intruder detection, Intruder selfie, Panic mode (shake to close), Prevent screenshots, Prevent app uninstall |
| Storage | Cloud backup (auto status), Trash |
| General | Notifications, Feedback, Rate (Play In-App Review), Share, Privacy Policy, Terms, version label |

`SettingsDestination` values are mapped to routes in `navigation/SettingsNavigation.kt`.

## App icon disguise (`AppIconScreen`)

`AppIconSwitcherImpl` enables exactly one launcher `<activity-alias>` (Default, Calculator 1–4,
Music 1–4, Notes 1–4, Maps 1–4). It enables the new alias **before** disabling the old ones, so the
app always has a launcher entry.

## Uninstall protection

`VaultDeviceAdminReceiver` is a device admin used only to block uninstall: while it is active,
Android refuses to uninstall the app. `UninstallProtectionSheet` explains this before the system
activation screen opens.

## Structure

| Package | Classes |
|---|---|
| `domain/repository` | `DeviceFeatures` (`AppIconSwitcher`, `UninstallProtection`) |
| `data` | `AppIconSwitcherImpl`, `UninstallProtectionImpl`, `VaultDeviceAdminReceiver` |
| `domain/usecase` | `ObserveSettingsOverviewUseCase`, `UpdateSettingsUseCase`, `FingerprintUnlockUseCase`, `ChangeAppIconUseCase` |
| `presentation/mvi` | `SettingsMvi` / `SettingsViewModel`, `AppIconMvi` / `AppIconViewModel` |
| `presentation/ui` | `SettingsContent`, `AppIconScreen` |
| `presentation/components` | `UninstallProtectionSheet` |

## Manifest

Declares `VaultDeviceAdminReceiver` (`BIND_DEVICE_ADMIN`) with its policy in `res/xml/device_admin.xml`.

## Dependencies

Standard feature dependencies come from `calculatorvault.android.feature`: `:core:common`, `:core:model`, `:core:designsystem`, `:core:ui`, `:core:navigation`, `:core:domain`, Lifecycle Compose, Navigation 3, kotlinx.serialization and Koin Compose.

Additional dependencies: Activity Compose, Play In-App Review.
