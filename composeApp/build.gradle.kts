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
            api(project(":core-data")) // Exposed in SharedAppSpec
            implementation("org.jetbrains.compose.ui:ui-backhandler:1.8.0")
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.redwood.widget)
            api(libs.redwood.treehouse.host) // Exposed in SharedAppSpec
            api(libs.redwood.treehouse) // Exposed in SharedAppSpec
            implementation(libs.redwood.compose)
            implementation(libs.redwood.treehouse.host.composeui)
            api(project(":shared"))
            api(project(":shared-widget"))
            api(project(":shared-protocol-host"))
            api(libs.zipline) // Exposed in SharedAppSpec
            api(libs.zipline.loader)
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
