plugins {
    alias(libs.plugins.cvAndroidLibrary)
    alias(libs.plugins.cvKoin)
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.model)
    api(libs.androidx.datastore.preferences)
}
