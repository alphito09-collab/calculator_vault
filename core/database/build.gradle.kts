plugins {
    alias(libs.plugins.cvAndroidLibrary)
    alias(libs.plugins.cvAndroidRoom)
    alias(libs.plugins.cvKoin)
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.model)
}
