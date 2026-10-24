# :feature:pin

**PIN creation and change.** Creates the vault PIN on first run, changes it from Settings, and sets a new one after recovery.

| | |
|---|---|
| Gradle path | `:feature:pin` |
| Namespace | `com.calculator.vault.feature.pin` |
| Plugins | `calculatorvault.android.feature` |
| Routes | `CreatePinRoute`, `ChangePinRoute`, `ResetPinRoute(viaRecovery)` |
| Koin module | `com.calculator.vault.feature.pin.di.PinModule` |
| Navigation | `EntryProviderScope<NavKey>.pinEntries(navigator)` in `navigation/` |

## Screens

| Screen | Flow |
|---|---|
| `CreatePinScreen` | First run, on the **calculator keypad**: type 4 digits and press `=`, then repeat to confirm. This creates the vault and continues to `RecoveryMethodRoute` |
| `PinPadScreen` (change) | Settings → Change PIN: current PIN → new PIN → confirm |
| `PinPadScreen` (reset) | After a recovery method or the reset code opened the vault: new PIN → confirm, then `navigator.openVault()` |

## Structure

| Package | Classes |
|---|---|
| `domain/usecase` | `CreateVaultUseCase`, `VerifyCurrentPinUseCase`, `ChangePinUseCase`, `ResetPinUseCase` |
| `presentation/mvi` | `CreatePinContract` / `CreatePinViewModel`, `PinPadContract` / `PinPadViewModel` |
| `presentation/components` | `PinSlots`: four underlined slots, a blue `*` per digit, red and shaking on error |

Changing the PIN re-wraps the existing master keys with the new PIN-derived key. Vault content is
not re-encrypted.

## Dependencies

Standard feature dependencies come from `calculatorvault.android.feature`: `:core:common`, `:core:model`, `:core:designsystem`, `:core:ui`, `:core:navigation`, `:core:domain`, Lifecycle Compose, Navigation 3, kotlinx.serialization and Koin Compose.
