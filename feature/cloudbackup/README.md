# :feature:cloudbackup

**Google Drive backup.** Signs in with Google and backs up the already-encrypted vault to the hidden Drive `appDataFolder`. It can also restore from there.

| | |
|---|---|
| Gradle path | `:feature:cloudbackup` |
| Namespace | `com.calculator.vault.feature.cloudbackup` |
| Plugins | `calculatorvault.android.feature` |
| Routes | `CloudBackupRoute` |
| Koin module | `com.calculator.vault.feature.cloudbackup.di.CloudBackupModule` |
| Navigation | `EntryProviderScope<NavKey>.cloudBackupEntries(navigator)` in `navigation/` |

## Behaviour

- **Sign in** uses Credential Manager and Google ID (`GoogleSignIn`), followed by a one-time
  Drive `appdata` consent (`DriveAuth` in `:core:network`).
- The status card shows "Backed up · Last backup Today, 09:12" with the Drive storage bar, or the
  running or failed state (no connection, auth required, quota exceeded).
- **Back up now**, **Restore**, **Auto backup** (daily `PeriodicWorkRequest`), and **Network**
  (Wi-Fi only, or Wi-Fi + cellular).
- An account menu with sign out.
- Uploads are incremental and done by `CloudBackupWorker` in `:core:data`. Data is encrypted
  before it leaves the device; Drive only ever stores ciphertext.

## Configuration

Set the OAuth **Web application** client ID in `local.properties`. It is exposed as
`BuildConfig.GOOGLE_WEB_CLIENT_ID`:

```properties
GOOGLE_WEB_CLIENT_ID=1234567890-abc.apps.googleusercontent.com
```

## Structure

| Package | Classes |
|---|---|
| `data` | `GoogleSignIn` (Credential Manager account picker; needs an Activity context) |
| `domain/usecase` | `ObserveCloudBackupUseCase`, `CloudBackupActionsUseCase` |
| `presentation/mvi` | `CloudBackupMvi` (`CloudBackupState`, `CloudBackupIntent`, `CloudBackupEffect`, `CloudBackupViewModel`) |
| `presentation/ui` | `CloudBackupScreen` |
| `presentation/components` | `BackupStatusCard` |

## Dependencies

Standard feature dependencies come from `calculatorvault.android.feature`: `:core:common`, `:core:model`, `:core:designsystem`, `:core:ui`, `:core:navigation`, `:core:domain`, Lifecycle Compose, Navigation 3, kotlinx.serialization and Koin Compose.

Additional dependencies: `:core:network`, Activity Compose, Credential Manager (+ Play Services), Google ID, Play Services Auth.
