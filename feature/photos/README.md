# :feature:photos

**Hidden photos.** A photo grid and a full-screen viewer for decrypted images.

| | |
|---|---|
| Gradle path | `:feature:photos` |
| Namespace | `com.calculator.vault.feature.photos` |
| Plugins | `calculatorvault.android.feature` |
| Routes | `PhotosRoute`, `PhotoViewerRoute(itemId, slideshow)` |
| Koin module | `com.calculator.vault.feature.photos.di.PhotosModule` |
| Navigation | `EntryProviderScope<NavKey>.photosEntries(navigator)` in `navigation/` |

## Screens

- **`PhotosScreen`**: uses the shared gallery from `:core:ui`. It has a grid with 2, 3 or 4
  columns, optional grouping by date, multi-select, share, move to trash and unhide.
- **`PhotoViewerScreen`**: a horizontal pager of full-resolution images, decrypted in memory by
  Coil. It supports:
  - `ZoomableImage`: pinch and double-tap zoom with panning. Gestures are consumed only while zoomed, so the
    pager can still swipe at 1×.
  - a menu with rename, details, slideshow, share, delete (to trash) and unhide.
  - a slideshow with an interval picker (`IntervalPickerDialog`, remembered by `SlideshowIntervalUseCase`).
  - immersive mode: tap to hide or show the controls.

## Structure

| Package | Classes |
|---|---|
| `domain/usecase` | `SlideshowIntervalUseCase` |
| `presentation/mvi` | `PhotosViewModel`, `PhotoViewerMvi` / `PhotoViewerViewModel` |
| `presentation/ui` | `PhotosScreen`, `PhotoViewerScreen` |
| `presentation/components` | `ZoomableImage`, `IntervalPickerDialog` |

## Dependencies

Standard feature dependencies come from `calculatorvault.android.feature`: `:core:common`, `:core:model`, `:core:designsystem`, `:core:ui`, `:core:navigation`, `:core:domain`, Lifecycle Compose, Navigation 3, kotlinx.serialization and Koin Compose.

Additional dependencies: Activity Compose.
