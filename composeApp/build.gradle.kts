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
        }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
        }

        commonMain {
            dependencies {
            api(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.redwood.widget)
            implementation(libs.redwood.treehouse.host)
            implementation(libs.redwood.treehouse)
            implementation(libs.redwood.compose)
            implementation(libs.redwood.treehouse.host.composeui)
            api(project(":shared")) // Use api() for iOS framework export
            api(project(":shared-widget")) // Use api() for iOS framework export
            implementation(libs.zipline)
            implementation(libs.zipline.loader)
            implementation(libs.androidx.lifecycle.runtimeCompose)
        }
    }
        
    commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

