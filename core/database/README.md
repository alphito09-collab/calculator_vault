# :core:database

The Room database `vault_index.db`. It is a **rebuildable index** for fast queries. The source of truth is the encrypted metadata sidecars in the vault folder, so the database is excluded from backup and uses destructive migration.

| | |
|---|---|
| Gradle path | `:core:database` |
| Namespace | `com.calculator.vault.core.database` |
| Plugins | `calculatorvault.android.library`, `calculatorvault.android.room`, `calculatorvault.koin` |

## Schema (version 1)

| Entity | DAO | Purpose |
|---|---|---|
| `VaultItemEntity` | `VaultItemDao` | One row per hidden item: type, name, size, dates, trash state, etc. Also counts per `MediaType` (`TypeCount`) |
| `IntruderAttemptEntity` | `IntruderDao` | Logged failed-unlock bursts (timestamp, attempts, whether a photo exists) |
| `BackupFileEntity` | `BackupFileDao` | Files already uploaded to Drive, so backups are incremental |

Exported schemas are written to `schemas/` (configured by `calculatorvault.android.room`).
`Converters` stores `MediaType` as its name.

## Dependencies

`:core:common`, `:core:model`, Room runtime/KTX + compiler (KSP).
