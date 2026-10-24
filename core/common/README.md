# :core:common

Small cross-cutting utilities that every other module may use. It has no UI and no dependencies on other project modules.

| | |
|---|---|
| Gradle path | `:core:common` |
| Namespace | `com.calculator.vault.core.common` |
| Plugins | `calculatorvault.android.library`, `calculatorvault.koin` |

## Contents

| Package | Class | Purpose |
|---|---|---|
| `.` | `VaultConstants` | Vault folder name (`.calculator_vault_data`), PIN length, intruder attempt limits (default 3, range 1–6), forgot-PIN thresholds, trash retention (30 days), support/legal URLs, launcher alias prefix |
| `dispatchers` | `VaultDispatchers` | Injectable `main` / `io` / `default` dispatchers, so blocking work stays off the main thread and tests can substitute them |
| `dispatchers` | `ApplicationScope` | Process-lifetime `CoroutineScope` for work that must outlive a screen (imports, backups, wiping keys). It logs failures instead of crashing |
| `result` | `Results.kt` | `runCatching`-style helper that **never swallows `CancellationException`** |
| `session` | `LockGuard` | Callers announce launches of system UI (file picker, settings, share sheet, device admin) so the next `onStop` doesn't lock the vault |
| `di` | `CommonModule` | Koin module |

## Dependencies

AndroidX Core KTX, Lifecycle Process.
