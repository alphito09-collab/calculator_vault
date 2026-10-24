# :feature:intruder

**Intruder detection.** Shows who tried to open the vault with a wrong PIN, and configures how intruders are detected.

| | |
|---|---|
| Gradle path | `:feature:intruder` |
| Namespace | `com.calculator.vault.feature.intruder` |
| Plugins | `calculatorvault.android.feature` |
| Routes | `IntruderRoute`, `IntruderPhotoRoute(attemptId)` |
| Koin module | `com.calculator.vault.feature.intruder.di.IntruderModule` |
| Navigation | `EntryProviderScope<NavKey>.intruderEntries(navigator)` in `navigation/` |

## Behaviour

- Toggles for **Intruder detection** and **Intruder selfie**. Turning on the selfie asks for the
  camera permission.
- **Wrong PIN limit**: a wheel from 1 to 6 (default 3). Wrong PINs below the limit count as typos.
- The log lists each burst ("3 failed attempts · Today, 14:22") with the captured photo, or a
  camera-off tile if there is none.
- Tap an entry to see the full photo. The photo is decrypted in memory, because selfies are
  encrypted to the vault's public key while it is locked.
- **Clear** deletes the log and its photos after confirmation.

The attempts themselves are recorded by `RecordIntruderUseCase` in `:feature:calculator`.

## Structure

| Package | Classes |
|---|---|
| `domain/usecase` | `ObserveIntruderOverviewUseCase`, `IntruderSettingsUseCase` |
| `presentation/mvi` | `IntruderMvi` (`IntruderState`, `IntruderIntent`, `IntruderEffect`, `IntruderViewModel`) |
| `presentation/ui` | `IntruderScreen` (log + photo screen) |
| `presentation/components` | Attempt card, wrong-PIN-limit wheel |

## Dependencies

Standard feature dependencies come from `calculatorvault.android.feature`: `:core:common`, `:core:model`, `:core:designsystem`, `:core:ui`, `:core:navigation`, `:core:domain`, Lifecycle Compose, Navigation 3, kotlinx.serialization and Koin Compose.
