plugins {
    alias(libs.plugins.cvAndroidLibrary)
    alias(libs.plugins.cvKoin)
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.model)
    implementation(libs.tink.android)
    implementation(libs.androidx.core.ktx)
}
