# :feature:onboarding

**First-run onboarding.** A three-page pager that introduces the vault before the user creates a PIN.

| | |
|---|---|
| Gradle path | `:feature:onboarding` |
| Namespace | `com.calculator.vault.feature.onboarding` |
| Plugins | `calculatorvault.android.feature` |
| Routes | `OnboardingRoute` |
| Koin module | `com.calculator.vault.feature.onboarding.di.OnboardingModule` |
| Navigation | `EntryProviderScope<NavKey>.onboardingEntries(navigator)` in `navigation/` |

## Structure

| Package | Classes |
|---|---|
| `domain/usecase` | `CompleteOnboardingUseCase` |
| `presentation/mvi` | `OnboardingContract`, `OnboardingViewModel` (guards against a double tap opening two PIN screens) |
| `presentation/ui` | `OnboardingScreen`: `HorizontalPager` with 3 pages. **Skip** appears on the first page only and fades out while swiping |
| `presentation/components` | `OnboardingIllustration` (full-bleed illustration with parallax that fades into the background), `PagerIndicator` (pill for the current page, dots for the rest) |

When the user finishes or skips, the app navigates to `CreatePinRoute`.

## Dependencies

Standard feature dependencies come from `calculatorvault.android.feature`: `:core:common`, `:core:model`, `:core:designsystem`, `:core:ui`, `:core:navigation`, `:core:domain`, Lifecycle Compose, Navigation 3, kotlinx.serialization and Koin Compose.
