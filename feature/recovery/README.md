# :feature:recovery

**PIN recovery.** Sets up recovery methods and uses them to open the vault when the PIN is forgotten.

| | |
|---|---|
| Gradle path | `:feature:recovery` |
| Namespace | `com.calculator.vault.feature.recovery` |
| Plugins | `calculatorvault.android.feature` |
| Routes | `RecoveryMethodRoute`, `PatternSetupRoute`, `RecoveryEmailRoute`, `EmailOtpRoute`, `SecurityQuestionRoute`, `ForgotPinRoute`, `VaultRecoverySettingsRoute` |
| Koin module | `com.calculator.vault.feature.recovery.di.RecoveryModule` |
| Navigation | `EntryProviderScope<NavKey>.recoveryEntries(navigator)` in `navigation/` |

## Recovery methods

| Method | Setup | Recovery | Survives reinstall |
|---|---|---|---|
| Fingerprint | Toggle and verify with `BiometricPrompt` | Fingerprint → set a new PIN | ❌ (Keystore-bound) |
| Pattern | `PatternSetupScreen` (also used for the App Lock pattern) | Draw the pattern | ✅ (wraps the master keys) |
| Security question | One of 6 questions plus an answer (normalized: trimmed, lower-case) | Answer the question | ✅ (wraps the master keys) |
| Email OTP | Verify the email with a 6-digit code | Code sent to the email → set a new PIN | ❌ (device key) |

## Structure

| Package | Classes |
|---|---|
| `domain/usecase` | `ObserveRecoveryOptionsUseCase`, `AvailableRecoveryMethodsUseCase`, `FingerprintRecoveryUseCase`, `SavePatternUseCase`, `UnlockWithPatternUseCase`, `SaveSecurityAnswerUseCase`, `UnlockWithAnswerUseCase`, `SendOtpUseCase`, `VerifyOtpUseCase`, `CompleteEmailRecoveryUseCase`, `FinishRecoveryPromptUseCase` |
| `presentation/mvi` | `RecoveryMethodViewModel`, `PatternSetupViewModel`, `SecurityQuestionViewModel`, `RecoveryEmailViewModel` / `EmailOtpViewModel`, `ForgotPinViewModel` |
| `presentation/ui` | `RecoveryMethodScreen`, `PatternSetupScreen`, `SecurityQuestionScreen`, `EmailScreens` (email + OTP), `ForgotPinScreen` |
| `presentation/components` | `RecoveryOptionCard`, `OtpInput` (six digit boxes backed by one hidden text field, works with autofill) |

On first run, recovery setup can be skipped. When opened from Settings, the same screens are
behind the PIN. A successful recovery resets to `ResetPinRoute`.

The OTP backend is configured with `OTP_BASE_URL` (see `:core:network`).

## Dependencies

Standard feature dependencies come from `calculatorvault.android.feature`: `:core:common`, `:core:model`, `:core:designsystem`, `:core:ui`, `:core:navigation`, `:core:domain`, Lifecycle Compose, Navigation 3, kotlinx.serialization and Koin Compose.
