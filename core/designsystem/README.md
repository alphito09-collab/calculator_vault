# :core:designsystem

The Calculator Vault design system: theme tokens, typography, icons and stateless Compose components built from the Figma design (see `design/figma-cache`). It doesn't know about vault data.

| | |
|---|---|
| Gradle path | `:core:designsystem` |
| Namespace | `com.calculator.vault.core.designsystem` |
| Plugins | `calculatorvault.android.library.compose` |

## Theme (`theme/`)

- `CalculatorVaultTheme(darkTheme)` provides the light and dark `VaultTheme.scheme` and
  wraps Material 3.
- `Color.kt`: brand palette (blue, navy, containers, semantic colours) for light and dark.
- `Type.kt`: **Inter** (light, regular, medium, semibold, bold) bundled in `res/font`.
- `Shape.kt` and `Spacing.kt`: corner radii and the spacing scale.

## Components (`component/`)

| File | Components |
|---|---|
| `Buttons.kt` | `VaultButton`, `VaultTextButton`, `VaultOutlinedChipButton`, `VaultIconButton`, `VaultFab` |
| `ActionBars.kt` | `FloatingActionBar`, `VaultBottomBar` |
| `TopBars.kt` | `VaultTopBar`, `SelectionTopBar` |
| `Cards.kt` / `Rows.kt` | `VaultCard`, `VaultCardColumn`, `SettingsRow`, `RowDivider`, `SectionLabel`, `IconTile` |
| `Controls.kt` | `VaultSwitch`, `VaultRadioButton`, `SelectionIndicator` |
| `Inputs.kt` | `VaultTextField`, `UnderlinedTextField`, `VaultSlider` |
| `Dialogs.kt` | `VaultDialog`, `VaultDialogContainer`, `VaultDialogBody`, `VaultBottomSheet` |
| `Feedback.kt` | `VaultSnackbarHost`, `VaultToastContent`, `TooltipBubble`, `showToast` |
| `Keypads.kt` | `PinPad`, `PinDots`, `KeypadKey`, `PatternLock` |
| `CalculatorKeypad.kt` | `CalculatorKeypad` (portrait + landscape) |
| `WheelPicker.kt` | Wheel picker (intruder limit, slideshow interval) |
| `Menus.kt` | `VaultDropdownMenu`, `VaultMenuItem` |
| `Progress.kt` | `VaultProgressBar`, `LoadingContent` |
| `EmptyState.kt` / `Banner.kt` | `EmptyState`, `InfoBanner` |
| `Pressable.kt` | `Modifier.scaleOnPress`, `rememberPressScale` |
| `AdaptiveLayout.kt` | `isLandscape()`, `isCompactHeight()`, `isExpandedWidth()`, `CenteredContent` |

## Icons and previews

- `icon/VaultIcons`: icons from the Figma export and Lucide, as vector drawables.
- `preview/Previews.kt`:
  - `@ThemePreviews`: light and dark component previews.
  - `@ScreenPreviews`: phone light, phone dark, landscape and tablet screen previews.

## Dependencies (exposed as `api`)

Compose Foundation, Material 3, UI, UI Graphics, Animation, Material 3 Adaptive.
