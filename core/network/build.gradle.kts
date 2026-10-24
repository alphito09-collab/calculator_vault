import java.util.Properties

plugins {
    alias(libs.plugins.cvAndroidLibrary)
    alias(libs.plugins.cvKoin)
    alias(libs.plugins.kotlin.serialization)
}

val localProperties = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use(::load)
}

android {
    buildFeatures.buildConfig = true
    defaultConfig {
        // Base URL of our own OTP backend (to be built later). Override in local.properties.
        buildConfigField(
            "String",
            "OTP_BASE_URL",
            "\"${localProperties.getProperty("OTP_BASE_URL", "https://api.example.com/")}\"",
        )
    }
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.model)
    implementation(projects.core.domain)
    implementation(libs.androidx.core.ktx)
    api(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.google.play.services.auth)
}
