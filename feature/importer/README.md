# :feature:importer

**Hide media from the device.** Lists device photos, videos, audio and documents so the user can select items and encrypt them into the vault.

| | |
|---|---|
| Gradle path | `:feature:importer` |
| Namespace | `com.calculator.vault.feature.importer` |
| Plugins | `calculatorvault.android.feature` |
| Routes | `ImporterRoute(filter: MediaType?)` |
| Koin module | `com.calculator.vault.feature.importer.di.ImporterModule` |
| Navigation | `EntryProviderScope<NavKey>.importerEntries(navigator)` in `navigation/` |

## Behaviour

- When opened for one type (from Home or the **+** menu) it lists only that type. With `null`
  it shows "All" with filter chips.
- Photos and videos come from MediaStore and are grouped by album. Android 14's partial "Select
  photos and videos" access counts as access.
- Documents come from the system file picker (**Browse device storage**) and need All-files access.
- **Permissions.** The prompt opens by itself at most once per visit. This avoids a loop when
  access is granted but the device has nothing of that type. A permanently denied permission links
  to the app's settings.
- **Hide** encrypts the selection (progress dialog), then removes the originals (see
  `OriginalsRemover` in `:core:data`).

## Structure

| Package | Classes |
|---|---|
| `domain/usecase` | `LoadDeviceAlbumsUseCase`, `ResolvePickedDocumentsUseCase`, `MediaAccessUseCase` |
| `presentation/mvi` | `ImporterMvi` (`ImporterState`, `ImporterIntent`, `ImporterEffect`), `ImporterViewModel` |
| `presentation/ui` | `ImporterScreen` |
| `presentation/components` | Browse-storage button, selectable rows for audio and documents |

## Dependencies

Standard feature dependencies come from `calculatorvault.android.feature`: `:core:common`, `:core:model`, `:core:designsystem`, `:core:ui`, `:core:navigation`, `:core:domain`, Lifecycle Compose, Navigation 3, kotlinx.serialization and Koin Compose.

Additional dependencies: Activity Compose (permission and document-picker launchers).

## Tests

`ImporterViewModelTest` (MockK + Turbine + coroutines-test). Run it with `./gradlew :feature:importer:testDebugUnitTest`.
