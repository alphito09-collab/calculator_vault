plugins {
    alias(libs.plugins.cvAndroidLibraryCompose)
}

dependencies {
    api(projects.core.designsystem)
    api(projects.core.model)
    api(projects.core.common)
    api(projects.core.domain)

    api(libs.androidx.lifecycle.viewmodel.compose)
    api(libs.androidx.lifecycle.runtime.compose)
    api(libs.coil.compose)
    api(libs.androidx.biometric)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
}
