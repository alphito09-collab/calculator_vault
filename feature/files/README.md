# :feature:files

**Hidden documents.** A document list and an in-app viewer.

| | |
|---|---|
| Gradle path | `:feature:files` |
| Namespace | `com.calculator.vault.feature.files` |
| Plugins | `calculatorvault.android.feature` |
| Routes | `FilesRoute`, `FileViewerRoute(itemId)` |
| Koin module | `com.calculator.vault.feature.files.di.FilesModule` |
| Navigation | `EntryProviderScope<NavKey>.filesEntries(navigator)` in `navigation/` |

## Screens

- **`FilesScreen`**: the shared gallery list with `FileTypeIcon` (PDF, Word, Excel, …).
- **`FileViewerScreen`** renders by type:
  - **PDF**: `PdfDocumentRenderer` wraps `PdfRenderer`. Pages are rendered lazily at screen
    width, behind a mutex (PdfRenderer allows one open page at a time), with a byte-bounded LRU
    page cache.
  - **Text**: shown inline, truncated for very large files.
  - **Image**: shown with Coil.
  - **Unsupported**: offers **Open with** another app, through a temporary decrypted copy that is
    wiped on lock.
  - Actions: share, delete, unhide.

## Structure

| Package | Classes |
|---|---|
| `domain/repository` | `DocumentRenderer` (one instance per open document) |
| `data` | `PdfDocumentRenderer` |
| `presentation/mvi` | `FilesViewModel`, `FileViewerViewModel` (`FileContent`: `Pdf`, `Text`, `Image`, `Unsupported`, `Error`) |
| `presentation/ui` | `FilesScreen`, `FileViewerScreen` |

## Dependencies

Standard feature dependencies come from `calculatorvault.android.feature`: `:core:common`, `:core:model`, `:core:designsystem`, `:core:ui`, `:core:navigation`, `:core:domain`, Lifecycle Compose, Navigation 3, kotlinx.serialization and Koin Compose.

Additional dependencies: Activity Compose.
