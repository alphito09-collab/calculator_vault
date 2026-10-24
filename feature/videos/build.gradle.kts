plugins {
    alias(libs.plugins.cvAndroidFeature)
}

dependencies {
    implementation(projects.core.media)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
}
