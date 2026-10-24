plugins {
    alias(libs.plugins.cvAndroidLibrary)
    alias(libs.plugins.cvKoin)
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.model)
    implementation(projects.core.security)
    implementation(projects.core.data)
    implementation(projects.core.domain)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.process)
    api(libs.coil.core)
    implementation(libs.coil.video)
    api(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.media3.session)
}
