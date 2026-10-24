# build-logic

An included Gradle build (`pluginManagement { includeBuild("build-logic") }`) with the
**convention plugins** that configure every module, following the Now in Android approach. Module
build files stay short: a feature module is usually just `plugins { alias(libs.plugins.cvAndroidFeature) }`.

| | |
|---|---|
| Module | `:convention` |
| Group | `com.calculator.vault.buildlogic` |
| Version catalog | Shared with the main build (`../gradle/libs.versions.toml`) |
| JVM target | 17 |

## Plugins

| Plugin ID | Catalog alias | Class | What it does |
|---|---|---|---|
| `calculatorvault.android.application` | `cvAndroidApplication` | `AndroidApplicationConventionPlugin` | AGP application, compile/min/target SDK, Java/Kotlin 17, `buildConfig`, Timber, JUnit |
| `calculatorvault.android.application.compose` | `cvAndroidApplicationCompose` | `AndroidApplicationComposeConventionPlugin` | Compose for the app: BOM, tooling, stability config, optional compiler reports |
| `calculatorvault.android.library` | `cvAndroidLibrary` | `AndroidLibraryConventionPlugin` | AGP library, namespace derived from the Gradle path, Timber, coroutines, test libraries (JUnit, coroutines-test, Turbine, MockK) |
| `calculatorvault.android.library.compose` | `cvAndroidLibraryCompose` | `AndroidLibraryComposeConventionPlugin` | Library + Compose |
| `calculatorvault.android.feature` | `cvAndroidFeature` | `AndroidFeatureConventionPlugin` | Library + Compose + Koin + serialization, plus the standard feature dependencies (see below) |
| `calculatorvault.android.room` | `cvAndroidRoom` | `AndroidRoomConventionPlugin` | Room Gradle plugin + KSP, schema directory `schemas/` |
| `calculatorvault.koin` | `cvKoin` | `KoinConventionPlugin` | Koin BOM, Koin Annotations + KSP compiler (`KOIN_CONFIG_CHECK=false`, `KOIN_DEFAULT_MODULE=false`) |
| `calculatorvault.jvm.library` | `cvJvmLibrary` | `JvmLibraryConventionPlugin` | Pure Kotlin/JVM module (used by `:core:model`) |

### What `calculatorvault.android.feature` adds

```kotlin
implementation(project(":core:common"))
implementation(project(":core:model"))
implementation(project(":core:designsystem"))
implementation(project(":core:ui"))
implementation(project(":core:navigation"))
implementation(project(":core:domain"))
implementation(libs.androidx.lifecycle.runtime.compose)
implementation(libs.androidx.lifecycle.viewmodel.compose)
implementation(libs.androidx.navigation3.runtime)
implementation(libs.kotlinx.serialization.json)
implementation(libs.koin.androidx.compose)
```

## Helpers (`com.calculator.vault.buildlogic`)

- `KotlinAndroid.kt`: `configureKotlinAndroid()` (SDK levels, Java 17, packaging excludes) and
  `configureKotlinJvm()`.
- `AndroidCompose.kt`: `configureAndroidCompose()` (Compose plugin, BOM, tooling, stability file
  `compose_compiler_config.conf`, and reports when `enableComposeCompilerReports=true`).
- `ProjectExtensions.kt`: `libs`, `lib(alias)`, `versionInt(alias)` and `defaultNamespace()`
  (`:feature:photos` → `com.calculator.vault.feature.photos`).

## Adding a module

1. Create `feature/<name>/build.gradle.kts` with `plugins { alias(libs.plugins.cvAndroidFeature) }`.
2. Add `include(":feature:<name>")` to `settings.gradle.kts`.
3. Add a `@Module @ComponentScan` class in `di/` and include it in `:app`'s `AppModule`.
4. Add `<name>Entries(navigator)` to the `entryProvider` in `VaultApp`.
