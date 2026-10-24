plugins {
    alias(libs.plugins.cvAndroidLibrary)
    alias(libs.plugins.cvKoin)
}

dependencies {
    api(projects.core.model)
    implementation(projects.core.common)
}
