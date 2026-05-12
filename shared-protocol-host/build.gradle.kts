plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.redwood.generator.protocol.host)
}

redwoodSchema {
    source = project(":schema")
    type = "com.example.serverdrivenui.schema.SduiSchema"
}

kotlin {
    iosArm64()
    iosSimulatorArm64()
    jvm()
    
    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared"))
            implementation(project(":shared-widget")) // Interface for Host widgets
            implementation(libs.redwood.protocol.host)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            // kotlinx-serialization-json is pulled in transitively via
            // redwood.protocol.host (used at runtime to decode ModifierElement
            // JSON payloads). We re-declare it here so test code can construct
            // JsonObject inputs without depending on a transitive coordinate.
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
        }
    }
}
