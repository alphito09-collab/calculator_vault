plugins {
    alias(libs.plugins.cvAndroidLibrary)
    alias(libs.plugins.cvKoin)
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.process)
}
