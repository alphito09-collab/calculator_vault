# :core:datastore

Jetpack DataStore Preferences storage, split by whether the data may leave the device.

| | |
|---|---|
| Gradle path | `:core:datastore` |
| Namespace | `com.calculator.vault.core.datastore` |
| Plugins | `calculatorvault.android.library`, `calculatorvault.koin` |

## Stores

| Class | File | Backed up | Contents |
|---|---|---|---|
| `VaultPreferences` | `files/datastore/vault_settings.preferences_pb` | ✅ Android Auto Backup | User-facing settings: theme, language, grid size, fingerprint, panic mode, screenshot blocking, intruder detection, selfie and limit, App Lock settings and locked apps, backup options, dismissed banners, onboarding progress |
| `DeviceState` | separate DataStore | ❌ excluded | Device/install-bound state: Keystore-wrapped convenience copies of the master keys (fingerprint, email recovery) and attempt counters. These can't move to another device |

`VaultPreferences` exposes typed setters, so callers never touch raw preference keys.

## Dependencies

`:core:common`, `:core:model`, DataStore Preferences (`api`).
