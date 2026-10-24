# :app

The application module. It builds the root Koin graph, hosts the only Activity, and puts every feature's navigation entries into one Navigation 3 `NavDisplay`.

| | |
|---|---|
| Gradle path | `:app` |
| Namespace | `com.calculator.vault.hide.app.lock.photo.video.free` |
| Plugins | `calculatorvault.android.application`, `calculatorvault.android.application.compose`, `calculatorvault.koin`, `kotlin.serialization` |

## Responsibilities

- **`CalculatorVaultApplication`**: starts Koin with `AppModule().module` and the WorkManager
  factory, plants Timber and enables StrictMode in debug builds only, starts `AppLockController`,
  and serves the vault-aware Coil `ImageLoader` (decrypting fetchers, no disk cache).
- **`MainActivity`**: the single `AppCompatActivity`. It owns the vault session rules:
  - leaving the app (Home, Recents, screen off) locks the vault and goes back to
    Splash → Calculator. After the next unlock the exact vault back stack is restored.
  - the vault never shows in Recents. `setRecentsScreenshotEnabled(false)` is set on API 33+,
    `FLAG_SECURE` is used below that, and a `PrivacyCover` is drawn the moment Recents or the
    notification shade takes focus.
  - it applies the **Prevent screenshots** setting (`FLAG_SECURE`).
  - in **panic mode**, a `ShakeDetector` (two strong shakes within 900 ms) closes the vault to the
    calculator.
  - it honours `LockGuard`, so the app's own system flows (pickers, permission screens, the share
    sheet) don't lock the vault.
- **`di/AppModule`**: the root `@Module` that includes every core and feature Koin module and
  provides the process-scoped `Navigator`.
- **`ui/VaultApp`**: theme, `NavDisplay` with the saveable-state and ViewModel-store entry
  decorators, RTL-aware slide/fade transitions (including predictive back), the app-wide toast
  host, and the privacy cover.

## Manifest highlights

- `MainActivity` is **not exported**. The launcher entries are 17 `<activity-alias>` disguises
  (`.alias.Default`, `Calculator1‥4`, `Music1‥4`, `Notes1‥4`, `Maps1‥4`), and exactly one is enabled.
- `FileProvider` (`${applicationId}.fileprovider`) for sharing decrypted temp copies.
- WorkManager's default initializer is removed, because Koin's `workManagerFactory()` initializes it.
- Auto Backup includes only `vault.json` and the settings DataStore
  (`res/xml/backup_rules.xml`, `data_extraction_rules.xml`).

## Build configuration

| Build type | Application ID suffix | Minify | Signing |
|---|---|---|---|
| `debug` | `.debug` | ❌ | debug key |
| `release` | none | R8 (`optimization.enable = true`) | `keystore.properties`, falling back to the debug key |

- `localeFilters` limits resources to `en, ar, de, es, fr, hi, in, pt, ru, tr, ur, zh`, and
  `generateLocaleConfig = true`.
- App-specific R8 rules live in `src/main/keepRules/rules.keep`. They strip `android.util.Log`
  and `Timber` calls and keep the Credential Manager Play Services provider.

## Dependencies

Every `:core:*` and `:feature:*` module, plus Activity Compose, SplashScreen, AppCompat,
Navigation 3, Lifecycle (process, viewmodel-navigation3), Material 3 Adaptive, WorkManager, Koin
(compose, workmanager), Coil, ProfileInstaller, and LeakCanary (debug only).
