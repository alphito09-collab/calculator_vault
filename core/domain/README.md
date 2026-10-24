# :core:domain

The domain layer shared across features: **repository interfaces** and **use cases** that more than one feature needs. It has no implementations and no framework types beyond what the interfaces require.

| | |
|---|---|
| Gradle path | `:core:domain` |
| Namespace | `com.calculator.vault.core.domain` |
| Plugins | `calculatorvault.android.library`, `calculatorvault.koin` |

## Repository interfaces (`repository/`)

| Interface | Implemented in `:core:data` / elsewhere |
|---|---|
| `AuthRepository` | Create vault, unlock with PIN/pattern/answer, biometrics, lock state |
| `VaultRepository` | Observe items, hide, unhide, rename, trash, restore, delete, decrypt for sharing |
| `DeviceMediaRepository` | Device albums, media and documents through MediaStore and the SAF picker |
| `StorageRepository` | All-files access, the restore-previous-vault flow |
| `RecoveryRepository` | Pattern, security question, email recovery setup and unlock |
| `OtpRepository` | Send and verify email OTP |
| `SettingsRepository` | `UserSettings` flow and setters |
| `AppLockRepository` | Locked apps, App Lock settings, unlock grace periods |
| `IntruderRepository` / `IntruderCamera` | Intruder log and the silent selfie (camera in `:core:media`) |
| `CloudBackupRepository` | Sign-in state, Drive backup and restore, schedule |
| `NetworkMonitor` | Online and unmetered state (in `:core:network`) |

## Shared use cases (`usecase/`)

- `VaultItemUseCases.kt`: `ObserveVaultItemsUseCase`, `ObserveVaultItemUseCase`,
  `ObserveVaultCountsUseCase`, `ObserveTrashUseCase`, `HideMediaUseCase`, `UnhideItemsUseCase`,
  `MoveToTrashUseCase`, `RestoreFromTrashUseCase`, `DeletePermanentlyUseCase`, `EmptyTrashUseCase`,
  `DecryptForSharingUseCase`, `AddCaptureUseCase`, `RenameItemUseCase`.
- `ValidationUseCases.kt`: `ValidateEmailUseCase`, `UnlockWithPinUseCase`.
- `GroupByDateUseCase`: groups items into Today, Yesterday and dated sections for the grids.

Use cases needed by only one feature live in that feature's `domain/usecase` package instead.

## Dependencies

`:core:model` (`api`), `:core:common`.
