# :feature:splash

**Launch screen and start-up routing.** Shows the disguise's icon and label while the app starts. On a first install it shows the "Get Started" welcome instead.

| | |
|---|---|
| Gradle path | `:feature:splash` |
| Namespace | `com.calculator.vault.feature.splash` |
| Plugins | `calculatorvault.android.feature` |
| Routes | `SplashRoute` |
| Koin module | `com.calculator.vault.feature.splash.di.SplashModule` |
| Navigation | `EntryProviderScope<NavKey>.splashEntries(navigator)` in `navigation/` |

## Flow

```
First install:  Splash (Get Started) ─▶ Language ─▶ Onboarding ─▶ Create PIN ─▶ Recovery setup ─▶ Vault
Vault exists:   Splash (disguise icon animation) ─▶ Calculator
```

`GetStartDestinationUseCase` resumes at the first unfinished step (`WELCOME`, `LANGUAGE`,
`ONBOARDING`, `CREATE_PIN`). Once a vault exists, the destination is always `CALCULATOR`.

## Structure

| Package | Classes |
|---|---|
| `domain/usecase` | `GetStartDestinationUseCase`, `AcceptWelcomeUseCase` ("Get Started" accepts the Privacy Policy and Terms) |
| `presentation/mvi` | `SplashContract` (`LOADING` / `WELCOME` / disguise phases), `SplashViewModel` |
| `presentation/ui` | `SplashScreen`: the disguise icon pops in and its label fades up. It waits for window focus, because Recents keeps the activity resumed |
| `presentation/components` | `GetStartedContent`: logo, title, Get Started button, legal links |

Language is **pushed** on top of the welcome screen rather than replacing it, so Back from the
language list returns to the welcome.

## Dependencies

Standard feature dependencies come from `calculatorvault.android.feature`: `:core:common`, `:core:model`, `:core:designsystem`, `:core:ui`, `:core:navigation`, `:core:domain`, Lifecycle Compose, Navigation 3, kotlinx.serialization and Koin Compose.
