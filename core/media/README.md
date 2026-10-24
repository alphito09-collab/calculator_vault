# :core:media

Media plumbing that reads encrypted vault content directly. Decrypted bytes stay in memory and are never written to a disk cache.

| | |
|---|---|
| Gradle path | `:core:media` |
| Namespace | `com.calculator.vault.core.media` |
| Plugins | `calculatorvault.android.library`, `calculatorvault.koin` |

## Images (`image/`)

- `VaultImageLoader`: the app-wide Coil 3 `ImageLoader`, installed through
  `SingletonImageLoader.Factory` in `:app`. It uses decrypting fetchers, **no disk cache**, and a
  bounded memory cache that is cleared when the vault locks.
- `VaultImageFetchers`: `VaultThumbnailFetcher`, `VaultImageFetcher`, `IntruderPhotoFetcher` and
  `AppIconFetcher`, keyed by the request types in `:core:model`.

## Playback (`player/`)

- `EncryptedDataSource`: a Media3 `DataSource` that streams `vault://item/<id>` by decrypting
  segments on demand, so seeking is instant.
- `VaultPlayerFactory`: builds ExoPlayers that read from the encrypted data source.
- `AudioController`: one shared audio player, so playback continues between the list, the
  mini-player and the full player. It handles queue, shuffle, `RepeatMode`, speed, and is stopped
  on lock.
- `AudioEffects`: optional equalizer bands, bass boost, virtualizer and `ReverbPreset` for the
  audio session. Unsupported effects on a device just disable their control.

## Camera (`camera/`)

`CameraXIntruderCamera` implements `IntruderCamera`. It captures one front-camera still with no
preview, bound to the process lifecycle, and the photo is encrypted with the vault's public key.

## Dependencies

`:core:common`, `:core:model`, `:core:security`, `:core:data`, `:core:domain`, Coil 3 (`api`) +
coil-video, Media3 ExoPlayer (`api`) + Session, CameraX core/camera2/lifecycle, Lifecycle Process.
