plugins {
    alias(libs.plugins.cvAndroidLibraryCompose)
}

dependencies {
    api(libs.androidx.compose.foundation)
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.ui.graphics)
    api(libs.androidx.compose.animation)
    api(libs.androidx.compose.material3.adaptive)
    implementation(libs.androidx.compose.ui.util)
    implementation(libs.androidx.core.ktx)
}
