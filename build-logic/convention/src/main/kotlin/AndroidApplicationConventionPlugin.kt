import com.android.build.api.dsl.ApplicationExtension
import com.calculator.vault.buildlogic.configureKotlinAndroid
import com.calculator.vault.buildlogic.lib
import com.calculator.vault.buildlogic.libs
import com.calculator.vault.buildlogic.versionInt
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.application")

            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)
                defaultConfig.targetSdk = libs.versionInt("targetSdk")
                defaultConfig.testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                defaultConfig.vectorDrawables.useSupportLibrary = true
                testOptions.animationsDisabled = true
                buildFeatures.buildConfig = true
            }

            dependencies {
                "implementation"(libs.lib("timber"))
                "testImplementation"(libs.lib("junit"))
            }
        }
    }
}
