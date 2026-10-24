# :feature:calculator

**The calculator lock screen.** A real, working calculator that doubles as the PIN pad.

| | |
|---|---|
| Gradle path | `:feature:calculator` |
| Namespace | `com.calculator.vault.feature.calculator` |
| Plugins | `calculatorvault.android.feature` |
| Routes | `CalculatorRoute` |
| Koin module | `com.calculator.vault.feature.calculator.di.CalculatorModule` |
| Navigation | `EntryProviderScope<NavKey>.calculatorEntries(navigator)` in `navigation/` |

## Behaviour

- `CalculatorEngine`: an immutable four-function calculator with operator precedence (× ÷ before
  + −), `%`, `±`, an expression line ("12 + 7 ×"), digit grouping, and scientific notation for very
  large or small numbers.
- **Unlocking.** The PIN unlocks only when it is typed on its own and followed by `=`. Anything
  else behaves like a normal calculator (`1234 =` just shows `1234`).
- **Fingerprint unlock**, if enabled, uses `BiometricPrompt` with the Keystore biometric key.
- **Forgot PIN?** (`ForgotPinPromptUseCase`) appears after 3 wrong PINs. With intruder detection on,
  it appears after the intruder limit + 2, so the intruder is logged first. It requires at least
  one configured recovery method. Long-pressing `=` offers every method.
- **Reset code.** A hidden code (`VaultConstants.RESET_PIN_CODE`) followed by `=` opens
  "set a new PIN", but only on the same install, because it needs the device-bound key
  (`ResetPinWithCodeUseCase`).
- **Intruder logging.** `RecordIntruderUseCase` logs a burst once it reaches the user's limit
  (1–6). It takes one front-camera selfie per burst when that option is on and the camera
  permission is granted.

## Structure

| Package | Classes |
|---|---|
| `domain/engine` | `CalculatorEngine`, `CalcState` |
| `domain/usecase` | `ForgotPinPromptUseCase`, `ResetPinWithCodeUseCase`, `RecordIntruderUseCase` |
| `presentation/mvi` | `CalculatorContract`, `CalculatorViewModel` |
| `presentation/ui` | `CalculatorScreen` (portrait and landscape keypad) |
| `presentation/components` | `CalculatorDisplay` (result text shrinks to fit), `ForgotPinDialog` |

## Dependencies

Standard feature dependencies come from `calculatorvault.android.feature`: `:core:common`, `:core:model`, `:core:designsystem`, `:core:ui`, `:core:navigation`, `:core:domain`, Lifecycle Compose, Navigation 3, kotlinx.serialization and Koin Compose.

Additional dependencies: AndroidX Biometric.

## Tests

`CalculatorEngineTest`, `ForgotPinPromptUseCaseTest`. Run them with `./gradlew :feature:calculator:testDebugUnitTest`.
