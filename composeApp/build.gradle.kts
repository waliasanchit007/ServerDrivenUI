import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.zipline)
}

kotlin {
    androidLibrary {
        namespace = "com.example.serverdrivenui.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            binaryOption("bundleId", "com.example.serverdrivenui")
            // Export all transitive dependencies
            export(project(":shared"))
            export(project(":shared-widget"))
            // Link against SQLite (required by Zipline caching)
            linkerOpts("-lsqlite3")
        }
        iosTarget.compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    freeCompilerArgs.add("-opt-in=app.cash.redwood.RedwoodCodegenApi")
                    freeCompilerArgs.add("-opt-in=androidx.compose.ui.ExperimentalComposeUiApi")
                }
            }
        }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
        }

        commonMain.dependencies {
            api(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation("org.jetbrains.compose.ui:ui-backhandler:1.8.0")  // Cross-platform BackHandler
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.redwood.widget)
            implementation(libs.redwood.treehouse.host)
            implementation(libs.redwood.treehouse)
            implementation(libs.redwood.compose)
            implementation(libs.redwood.treehouse.host.composeui)
            api(project(":shared")) // Use api() for iOS framework export
            api(project(":shared-widget")) // Use api() for iOS framework export
            api(project(":shared-protocol-host")) // For SduiSchemaHostProtocol
            implementation(libs.zipline)
            implementation(libs.zipline.loader)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.coil.compose)
            // Ktor HTTP Client core
            implementation(libs.ktor.client.core)
        }
        
        iosMain.dependencies {
            // Ktor Darwin engine for iOS HTTP client
            implementation(libs.ktor.client.darwin)
        }
            
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
