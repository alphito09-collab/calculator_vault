# :core:ui

Shared presentation code that depends on vault models: the MVI base, the shared gallery screen logic, vault-aware composables, biometrics and UI utilities. It sits between the design system and the feature modules.

| | |
|---|---|
| Gradle path | `:core:ui` |
| Namespace | `com.calculator.vault.core.ui` |
| Plugins | `calculatorvault.android.library.compose` |

## MVI (`mvi/Mvi.kt`)

```kotlin
interface UiState; interface UiIntent; interface UiEffect

abstract class MviViewModel<S : UiState, I : UiIntent, E : UiEffect>(initialState: S) : ViewModel() {
    val state: StateFlow<S>
    val effects: Flow<E>          // buffered channel, delivered once
    fun onIntent(intent: I)
    protected fun setState(reducer: S.() -> S)
    protected fun sendEffect(effect: E)
}

@Composable fun <E> Flow<E>.CollectEffects(onEffect: suspend (E) -> Unit) // collects while STARTED
```

## Gallery (`gallery/`)

`GalleryMvi` and `GalleryScaffold` hold the logic shared by the photo and video grids and the
audio and file lists: selection, select all, share, move to trash, unhide, grid size, and
date grouping. Feature modules subclass the ViewModel with `@KoinViewModel`.

## Components (`component/`)

`MediaGrid` (`SelectableMediaGrid`), `MediaThumbnail`, `VaultItemRow`, `FileTypeIcon`,
`AppIconArt`, `PrivacyCover`, `ResetCodeBanner`, `ExitAppDialog`, `RecoveryMethodUi`,
`ScreenLayouts` (`VaultScreen`, `BottomFade`, `OfflineBanner`), and `VaultDialogs`
(`ConfirmBulkActionDialog`, `TransferProgressDialog`, `RenameDialog`, `DetailsDialog`,
`FilesPermissionDialog`).

## Utilities

- `biometric/BiometricPrompter`: `BiometricPrompt` with a Keystore `CryptoObject`.
- `util/AppToasts`: app-wide toast event bus, collected by `VaultApp`.
- `util/ExternalIntents`: share, open with, and app or system settings (each one announced to
  `LockGuard`).
- `util/Formatters`: sizes, durations and dates.
- `util/Immersive` and `util/SystemBars`: system bar appearance and immersive viewers.

## Dependencies (exposed as `api`)

`:core:designsystem`, `:core:model`, `:core:common`, `:core:domain`, Lifecycle ViewModel and
runtime Compose, Coil Compose, AndroidX Biometric.
