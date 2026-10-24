# :core:data

The data layer. It implements every shared repository from `:core:domain` on top of the encrypted file store, Room, DataStore, Tink and Ktor. Only `:app` and `:core:media` depend on it; feature modules see only the domain interfaces.

| | |
|---|---|
| Gradle path | `:core:data` |
| Namespace | `com.calculator.vault.core.data` |
| Plugins | `calculatorvault.android.library`, `calculatorvault.koin`, `kotlin.serialization` |

## Vault storage (`storage/`)

Encrypted on-disk layout (`VaultFileStore`):

```
<vault root>/
├── vault.json            # VaultConfig: wrapped master keys only (also kept privately + Auto Backup)
├── items/<id>.cvx        # Content, Tink streaming AEAD (associated data = id), seekable
├── items/<id>.cvm        # Metadata JSON (ItemMeta), AEAD. The Room index is rebuilt from these
├── thumbs/<id>.cvt       # Thumbnail JPEG, AEAD
└── .nomedia              # Keeps gallery apps and the media scanner out
```

- `VaultPaths`: the primary root is `/storage/emulated/0/.calculator_vault_data` (`_debug` for
  debug builds), so the vault **survives uninstall**. Until All-files access is granted, content
  goes to an app-specific fallback folder and is migrated later. It also never writes into a
  shared folder that belongs to another (previous) vault.
- `AtomicFiles`: writes to `<file>.tmp`, syncs, then renames, so a crash never leaves a
  half-written file.
- `StorageAccess`: checks All-files access (API 30+) or legacy write permission.

## Configuration (`config/`)

- `VaultConfig` / `WrappedSecret`: vault ID, PBKDF2 salts, master keys wrapped per secret
  (PIN, pattern, answer), and the public hybrid keyset used for intruder photos.
- `VaultConfigStore`: persists the config privately and mirrors it into the shared folder. It
  can also restore it from there after a reinstall.

## Repositories (`repository/`)

`AuthRepositoryImpl`, `VaultRepositoryImpl`, `RecoveryRepositoryImpl`, `OtpRepositoryImpl`,
`SettingsRepositoryImpl`, `StorageRepositoryImpl`, `DeviceMediaRepositoryImpl`,
`AppLockRepositoryImpl`, `IntruderRepositoryImpl`, `CloudBackupRepositoryImpl`, plus:

- `VaultKeyring`: derives key-encryption keys off the main thread and wraps or unwraps the
  master keys.
- `VaultMaintenance`: runs after every unlock. It migrates fallback content to the shared
  folder, mirrors the config, rebuilds the index after a reinstall, and purges trash older than
  30 days.

## Media helpers (`media/`)

`ThumbnailFactory` (≤ max-edge JPEG previews, never decodes full-size bitmaps), `MediaRestorer`
(unhide back to public storage), `OriginalsRemover` (deletes originals after encryption).

## Background work (`work/`)

`CloudBackupWorker` uploads the **already encrypted** vault folder to Google Drive's
`appDataFolder` incrementally. It sends only new or changed files, deletes remote files that were
removed locally, and runs as a one-off or daily periodic job on Wi-Fi only or any network.

## Dependencies

`:core:domain` (`api`), `:core:common`, `:core:model`, `:core:datastore`, `:core:database`,
`:core:security`, `:core:network`, ExifInterface, WorkManager + Koin WorkManager,
kotlinx.serialization.
