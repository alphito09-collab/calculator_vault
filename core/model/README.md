# :core:model

Pure Kotlin domain models with **no Android dependencies**. They are immutable, and the Compose compiler treats them as stable (`com.calculator.vault.core.model.**` is listed in `compose_compiler_config.conf`).

| | |
|---|---|
| Gradle path | `:core:model` |
| Namespace | `com.calculator.vault.core.model` |
| Plugins | `calculatorvault.jvm.library` |

## Models

| File | Types |
|---|---|
| `VaultItem.kt` | `VaultItem`, `MediaType`, `VaultCounts` |
| `FileKind.kt` | `FileKind`: maps MIME types and extensions to document kinds (PDF, Word, Excel, …) |
| `DeviceMedia.kt` | `DeviceMedia`, `DeviceAlbum`, `SpecialAlbums`, `TransferProgress` |
| `Settings.kt` | `UserSettings`, `ThemeMode`, `GridSize` (2/3/4 columns), `AppLockSettings`, `LockAfter` (immediately, 30 s, 1 min, 5 min), `AppLockAuthMethod`, `BackupSettings`, `BackupNetwork` |
| `Security.kt` | `SecurityQuestion` (6 questions), `RecoveryOptions`, `RecoveryMethod` (fingerprint, pattern, email, security question), `PinCheckResult` |
| `Intruder.kt` | `IntruderAttempt`, `IntruderStats` |
| `Backup.kt` | `BackupStatus`, `BackupFailure`, `GoogleAccount`, `StorageQuota` |
| `InstalledApp.kt` | `InstalledApp` (App Lock list) |
| `AppIconOption.kt` | `AppIconOption`, `AppIconCategory`: launcher disguises and their alias names |
| `ImageRequests.kt` | Coil request keys: `VaultThumbnailRequest`, `VaultImageRequest`, `IntruderPhotoRequest`, `AppIconRequest` |

## Tests

`src/test/.../FileKindTest.kt`. Run it with `./gradlew :core:model:test`.
