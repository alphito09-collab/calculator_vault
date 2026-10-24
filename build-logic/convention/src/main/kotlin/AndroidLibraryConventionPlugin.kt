import com.android.build.api.dsl.LibraryExtension
import com.calculator.vault.buildlogic.configureKotlinAndroid
import com.calculator.vault.buildlogic.defaultNamespace
import com.calculator.vault.buildlogic.lib
import com.calculator.vault.buildlogic.libs
import com.calculator.vault.buildlogic.versionInt
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.library")

            extensions.configure<LibraryExtension> {
                if (namespace == null) namespace = defaultNamespace()
                configureKotlinAndroid(this)
                testOptions.targetSdk = libs.versionInt("targetSdk")
                testOptions.animationsDisabled = true
                defaultConfig.testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                // Every library exposes its own R class only.
                androidResources.enable = true
            }

            dependencies {
                "implementation"(libs.lib("timber"))
                "implementation"(libs.lib("kotlinx-coroutines-android"))
                "testImplementation"(libs.lib("junit"))
                "testImplementation"(libs.lib("kotlinx-coroutines-test"))
                "testImplementation"(libs.lib("turbine"))
                "testImplementation"(libs.lib("mockk"))
            }
        }
    }
}
