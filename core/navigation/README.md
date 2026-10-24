# :core:navigation

Navigation 3 destinations and the app's single back stack.

| | |
|---|---|
| Gradle path | `:core:navigation` |
| Namespace | `com.calculator.vault.core.navigation` |
| Plugins | `calculatorvault.android.library`, `kotlin.serialization`, `kotlin.compose` |

## `Routes.kt`

Every destination is a `@Serializable` `NavKey`:

- `AppRoute`: any destination.
- `VaultRoute : AppRoute`: shows **decrypted vault content**. These routes are reachable only while
  the vault is unlocked and are removed from the back stack on lock.

| Group | Routes |
|---|---|
| Launch / setup / lock | `SplashRoute`, `LanguageRoute`, `OnboardingRoute`, `CreatePinRoute`, `CalculatorRoute`, `ForgotPinRoute`, `ResetPinRoute` |
| Recovery setup | `RecoveryMethodRoute`, `PatternSetupRoute(PatternPurpose)`, `RecoveryEmailRoute`, `EmailOtpRoute(email, OtpPurpose)`, `SecurityQuestionRoute` |
| Vault | `MainRoute(MainTab)`, `ImporterRoute`, `PhotosRoute`, `PhotoViewerRoute`, `VideosRoute`, `VideoPlayerRoute`, `AudiosRoute`, `AudioPlayerRoute`, `EqualizerRoute`, `FilesRoute`, `FileViewerRoute`, `CameraRoute`, `TrashRoute`, `ThemesRoute`, `AppLockRoute`, `AppLockSettingsRoute`, `IntruderRoute`, `IntruderPhotoRoute`, `CloudBackupRoute`, `ChangePinRoute`, `ChangeAppIconRoute`, `VaultRecoverySettingsRoute` |

## `Navigator`

A Koin `@Single` that owns `backStack: SnapshotStateList<NavKey>`:

- `navigate`, `back`, `replaceTop`, `resetTo(vararg)`, `popUntil`.
- `lockVault()` saves the current `VaultRoute` entries and resets to `SplashRoute`.
- `openVault()` restores exactly that stack after unlock (or `MainRoute()` the first time).
- It is scoped to the **process**, so it survives configuration changes but is deliberately
  not restored after process death. A fresh process always starts locked, at the splash screen.
