# :feature:applock

**App Lock.** Locks other installed apps behind the vault PIN, fingerprint or a pattern.

| | |
|---|---|
| Gradle path | `:feature:applock` |
| Namespace | `com.calculator.vault.feature.applock` |
| Plugins | `calculatorvault.android.feature` |
| Routes | `AppLockRoute`, `AppLockSettingsRoute` |
| Koin module | `com.calculator.vault.feature.applock.di.AppLockModule` |
| Navigation | `EntryProviderScope<NavKey>.appLockEntries(navigator)` in `navigation/` |

## How it works

1. `AppLockController` (started from `Application.onCreate`) keeps `AppLockService` running
   **only** while App Lock is on, at least one app is locked, and usage access is granted.
2. `AppLockService` is a foreground service (`specialUse`). It uses `ForegroundAppDetector`
   (usage events) to watch which app comes to the foreground. Locking the screen re-locks every
   app immediately.
3. When a locked app opens, `LockOverlayActivity` appears over it. It runs in its own task and
   never shows in Recents. Leaving without unlocking sends the user to the home screen.
4. `BootReceiver` restarts App Lock after a reboot or an app update.

## Screens

- **`AppLockScreen`**: the installed app list with search, a lock toggle per app (the lock turns
  blue with a bounce), cards for the required permissions, and a first-use coach mark.
- **`AppLockSettingsScreen`**: enable or disable, **lock after** (immediately, 30 s, 1 min,
  5 min), and the unlock method (PIN, fingerprint, pattern). Pattern setup reuses `:feature:recovery`.

## Structure

| Package | Classes |
|---|---|
| `domain/repository` | `AppLockSystemRepository` (installed apps, special permissions) |
| `data` | `AppLockSystemRepositoryImpl` |
| `domain/usecase` | `ObserveAppLockSettingsUseCase`, `LoadInstalledAppsUseCase`, `AppLockPermissionsUseCase`, `ToggleAppLockedUseCase` (locking the first app also turns App Lock on), `UpdateAppLockSettingsUseCase`, `VerifyAppLockCredentialUseCase` |
| `presentation/mvi` | `AppLockMvi`: `AppLockListViewModel`, `AppLockSettingsViewModel` |
| `presentation/ui` | `AppLockScreen`, `AppLockSettingsScreen` |
| `overlay` | `LockOverlayActivity`, `LockOverlayScreen`, `LockOverlayViewModel` |
| `service` | `AppLockController`, `AppLockService`, `ForegroundAppDetector`, `BootReceiver` |

## Manifest

`PACKAGE_USAGE_STATS`, `SYSTEM_ALERT_WINDOW`, `FOREGROUND_SERVICE`,
`FOREGROUND_SERVICE_SPECIAL_USE`, `POST_NOTIFICATIONS`, `RECEIVE_BOOT_COMPLETED`, plus the service,
the overlay activity and the boot receiver.

## Dependencies

Standard feature dependencies come from `calculatorvault.android.feature`: `:core:common`, `:core:model`, `:core:designsystem`, `:core:ui`, `:core:navigation`, `:core:domain`, Lifecycle Compose, Navigation 3, kotlinx.serialization and Koin Compose.

Additional dependencies: Activity Compose, AppCompat, Lifecycle Service, Core KTX.
