# :feature:camera

**In-app camera.** Takes photos and records videos that go straight into the vault and never reach the public gallery.

| | |
|---|---|
| Gradle path | `:feature:camera` |
| Namespace | `com.calculator.vault.feature.camera` |
| Plugins | `calculatorvault.android.feature` |
| Routes | `CameraRoute(startWithVideo)` |
| Koin module | `com.calculator.vault.feature.camera.di.CameraModule` |
| Navigation | `EntryProviderScope<NavKey>.cameraEntries(navigator)` in `navigation/` |

## Behaviour

- CameraX preview with photo and video modes, front/back lens switching and flash.
- A capture is written to the app's private cache, **encrypted into the vault right away**
  (`AddCaptureUseCase`), and the plaintext is deleted.
- Shutter (`CameraControls`): a white ring. In video mode it shows a red dot that morphs into a
  rounded square while recording.
- When closed, it reports how many items were saved.

## Structure

| Package | Classes |
|---|---|
| `presentation/mvi` | `CameraMvi` (`CameraState`, `CameraIntent`, `CameraEffect`), `CameraViewModel` |
| `presentation/ui` | `CameraScreen` |
| `presentation/components` | `CameraControls` |

## Dependencies

Standard feature dependencies come from `calculatorvault.android.feature`: `:core:common`, `:core:model`, `:core:designsystem`, `:core:ui`, `:core:navigation`, `:core:domain`, Lifecycle Compose, Navigation 3, kotlinx.serialization and Koin Compose.

Additional dependencies: Activity Compose, CameraX (core, camera2, lifecycle, video, view).
