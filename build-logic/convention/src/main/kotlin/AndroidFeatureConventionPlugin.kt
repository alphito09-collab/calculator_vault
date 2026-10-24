import com.calculator.vault.buildlogic.lib
import com.calculator.vault.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * A feature module: `data/`, `domain/`, `presentation/{components,ui,mvi}` packages,
 * wired with Koin annotations and Navigation 3.
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("calculatorvault.android.library.compose")
            pluginManager.apply("calculatorvault.koin")
            pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")

            dependencies {
                "implementation"(project(":core:common"))
                "implementation"(project(":core:model"))
                "implementation"(project(":core:designsystem"))
                "implementation"(project(":core:ui"))
                "implementation"(project(":core:navigation"))
                "implementation"(project(":core:domain"))

                "implementation"(libs.lib("androidx-lifecycle-runtime-compose"))
                "implementation"(libs.lib("androidx-lifecycle-viewmodel-compose"))
                "implementation"(libs.lib("androidx-navigation3-runtime"))
                "implementation"(libs.lib("kotlinx-serialization-json"))
                "implementation"(libs.lib("koin-androidx-compose"))
            }
        }
    }
}
