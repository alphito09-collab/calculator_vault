# :feature:audios

**Hidden audio.** An audio list with a mini-player, a full player and an equalizer.

| | |
|---|---|
| Gradle path | `:feature:audios` |
| Namespace | `com.calculator.vault.feature.audios` |
| Plugins | `calculatorvault.android.feature` |
| Routes | `AudiosRoute`, `AudioPlayerRoute(itemId)`, `EqualizerRoute` |
| Koin module | `com.calculator.vault.feature.audios.di.AudiosModule` |
| Navigation | `EntryProviderScope<NavKey>.audiosEntries(navigator)` in `navigation/` |

## Screens

- **`AudiosScreen`**: the shared gallery list plus a **mini-player** pill that shows what's
  playing. Tap it to open the player, or use its button to pause.
- **`AudioPlayerScreen`**: play and pause, next and previous, a seek bar, shuffle, repeat
  (off, all, one), playback speed, a queue sheet, and a menu with rename, details, share, delete
  and unhide.
- **`EqualizerScreen`**: vertical band sliders, bass boost, virtualizer and reverb presets, using
  `AudioEffects` from `:core:media`.

Playback goes through the shared `AudioController` from `:core:media`, so it continues between
screens and stops when the vault locks.

## Structure

| Package | Classes |
|---|---|
| `presentation/mvi` | `AudiosViewModel`, `AudioPlayerViewModel`, `EqualizerViewModel` |
| `presentation/ui` | `AudiosScreen`, `AudioPlayerScreen`, `EqualizerScreen` |
| `presentation/components` | Mini-player, big play button, equalizer band slider |

## Dependencies

Standard feature dependencies come from `calculatorvault.android.feature`: `:core:common`, `:core:model`, `:core:designsystem`, `:core:ui`, `:core:navigation`, `:core:domain`, Lifecycle Compose, Navigation 3, kotlinx.serialization and Koin Compose.

Additional dependencies: `:core:media`, Activity Compose, Media3 ExoPlayer.
