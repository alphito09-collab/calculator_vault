import java.util.Properties

plugins {
    alias(libs.plugins.cvAndroidApplication)
    alias(libs.plugins.cvAndroidApplicationCompose)
    alias(libs.plugins.cvKoin)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.calculator.vault.hide.app.lock.photo.video.free"

    defaultConfig {
        applicationId = "com.calculator.vault.hide.app.lock.photo.video.free"
        versionCode = 1
        versionName = "1.0.0"
    }

    // Upload builds are signed from keystore.properties (git-ignored, never committed):
    // storeFile, storePassword, keyAlias, keyPassword.
    val keystoreProperties = rootProject.file("keystore.properties").takeIf { it.exists() }?.let { file ->
        Properties().apply { file.inputStream().use(::load) }
    }
    signingConfigs {
        if (keystoreProperties != null) {
            create("release") {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
        release {
            // Without keystore.properties the debug key signs release, so the R8 build can still
            // be installed and tested locally (as Now in Android does).
            signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
            optimization {
                enable = true
            }
        }
    }

    androidResources {
        // Only ship the translations we actually provide.
        localeFilters += listOf("en", "ar", "de", "es", "fr", "hi", "in", "pt", "ru", "tr", "ur", "zh")
        generateLocaleConfig = true
    }
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.model)
    implementation(projects.core.designsystem)
    implementation(projects.core.ui)
    implementation(projects.core.navigation)
    implementation(projects.core.datastore)
    implementation(projects.core.database)
    implementation(projects.core.security)
    implementation(projects.core.network)
    implementation(projects.core.media)
    implementation(projects.core.data)
    implementation(projects.core.domain)

    implementation(projects.feature.splash)
    implementation(projects.feature.language)
    implementation(projects.feature.onboarding)
    implementation(projects.feature.pin)
    implementation(projects.feature.recovery)
    implementation(projects.feature.calculator)
    implementation(projects.feature.home)
    implementation(projects.feature.importer)
    implementation(projects.feature.photos)
    implementation(projects.feature.videos)
    implementation(projects.feature.audios)
    implementation(projects.feature.files)
    implementation(projects.feature.camera)
    implementation(projects.feature.trash)
    implementation(projects.feature.settings)
    implementation(projects.feature.themes)
    implementation(projects.feature.applock)
    implementation(projects.feature.intruder)
    implementation(projects.feature.cloudbackup)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.adaptive)
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.profileinstaller)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.androidx.workmanager)
    implementation(libs.coil.compose)
    implementation(libs.kotlinx.serialization.json)

    debugImplementation(libs.leakcanary)

    testImplementation(libs.koin.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
