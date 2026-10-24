pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "CalculatorVault"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":app")

// Core
include(":core:common")
include(":core:model")
include(":core:designsystem")
include(":core:ui")
include(":core:navigation")
include(":core:datastore")
include(":core:database")
include(":core:security")
include(":core:network")
include(":core:media")
include(":core:data")
include(":core:domain")

// Features
include(":feature:splash")
include(":feature:language")
include(":feature:onboarding")
include(":feature:pin")
include(":feature:recovery")
include(":feature:calculator")
include(":feature:home")
include(":feature:importer")
include(":feature:photos")
include(":feature:videos")
include(":feature:audios")
include(":feature:files")
include(":feature:camera")
include(":feature:trash")
include(":feature:settings")
include(":feature:themes")
include(":feature:applock")
include(":feature:intruder")
include(":feature:cloudbackup")
