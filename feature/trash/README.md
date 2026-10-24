# :feature:trash

**Trash.** Items moved to the trash stay for **30 days** (`VaultConstants.TRASH_RETENTION_DAYS`). Then `VaultMaintenance` purges them.

| | |
|---|---|
| Gradle path | `:feature:trash` |
| Namespace | `com.calculator.vault.feature.trash` |
| Plugins | `calculatorvault.android.feature` |
| Routes | `TrashRoute` |
| Koin module | `com.calculator.vault.feature.trash.di.TrashModule` |
| Navigation | `EntryProviderScope<NavKey>.trashEntries(navigator)` in `navigation/` |

## Behaviour

- Multi-select, select all, **Restore**, **Delete forever**, and **Empty trash**. Every
  destructive action asks for confirmation.
- Shows a toast with the number of items restored or deleted.

## Structure

| Package | Classes |
|---|---|
| `presentation/mvi` | `TrashMvi` (`TrashState`, `TrashIntent`, `TrashEffect`), `TrashViewModel` |
| `presentation/ui` | `TrashScreen` |

It uses the shared `ObserveTrashUseCase`, `RestoreFromTrashUseCase`, `DeletePermanentlyUseCase` and
`EmptyTrashUseCase` from `:core:domain`.

## Dependencies

Standard feature dependencies come from `calculatorvault.android.feature`: `:core:common`, `:core:model`, `:core:designsystem`, `:core:ui`, `:core:navigation`, `:core:domain`, Lifecycle Compose, Navigation 3, kotlinx.serialization and Koin Compose.
