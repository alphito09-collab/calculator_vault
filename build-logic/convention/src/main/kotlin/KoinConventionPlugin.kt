import com.calculator.vault.buildlogic.lib
import com.calculator.vault.buildlogic.libs
import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/** Koin with compile-time generated modules (Koin Annotations + KSP). */
class KoinConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.google.devtools.ksp")

            dependencies {
                "implementation"(platform(libs.lib("koin-bom")))
                "implementation"(libs.lib("koin-core"))
                "implementation"(libs.lib("koin-android"))
                "implementation"(platform(libs.lib("koin-annotations-bom")))
                "implementation"(libs.lib("koin-annotations"))
                "ksp"(libs.lib("koin-ksp-compiler"))
                "testImplementation"(libs.lib("koin-test"))
            }

            extensions.configure<KspExtension> {
                // Definitions are split across Gradle modules, so per-module config checks would
                // report false negatives; the app module verifies the full graph in a unit test.
                arg("KOIN_CONFIG_CHECK", "false")
                arg("KOIN_DEFAULT_MODULE", "false")
            }
        }
    }
}
