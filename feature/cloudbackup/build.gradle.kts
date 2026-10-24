import java.util.Properties

plugins {
    alias(libs.plugins.cvAndroidFeature)
}

val localProperties = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use(::load)
}

android {
    buildFeatures.buildConfig = true
    defaultConfig {
        // OAuth "Web application" client ID from Google Cloud Console (needed for Google sign-in).
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"${localProperties.getProperty("GOOGLE_WEB_CLIENT_ID", "")}\"")
    }
}

dependencies {
    implementation(projects.core.network)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services)
    implementation(libs.google.identity.googleid)
    implementation(libs.google.play.services.auth)
}
