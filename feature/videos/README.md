# :feature:videos

**Hidden videos.** A video grid and an ExoPlayer player that streams encrypted files without writing plaintext to disk.

| | |
|---|---|
| Gradle path | `:feature:videos` |
| Namespace | `com.calculator.vault.feature.videos` |
| Plugins | `calculatorvault.android.feature` |
| Routes | `VideosRoute`, `VideoPlayerRoute(itemId)` |
| Koin module | `com.calculator.vault.feature.videos.di.VideosModule` |
| Navigation | `EntryProviderScope<NavKey>.videosEntries(navigator)` in `navigation/` |

## Screens

- **`VideosScreen`**: the shared gallery grid with duration badges, multi-select, share, trash
  and unhide.
- **`VideoPlayerScreen`**: a Media3 player built by `VaultPlayerFactory`. It reads through
  `EncryptedDataSource`, which decrypts segments on demand so seeking is instant. It has share and
  delete actions, and a controller that hides automatically.

`VideoPlayerViewModel` owns the `ExoPlayer` and releases it with the ViewModel. The ViewModel is
cleared when the vault locks, because vault routes are removed from the back stack.

## Structure

| Package | Classes |
|---|---|
| `presentation/mvi` | `VideosViewModel`, `VideoPlayerMvi` / `VideoPlayerViewModel` |
| `presentation/ui` | `VideosScreen`, `VideoPlayerScreen` |

## Dependencies

Standard feature dependencies come from `calculatorvault.android.feature`: `:core:common`, `:core:model`, `:core:designsystem`, `:core:ui`, `:core:navigation`, `:core:domain`, Lifecycle Compose, Navigation 3, kotlinx.serialization and Koin Compose.

Additional dependencies: `:core:media`, Activity Compose, Media3 ExoPlayer and UI.
