plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.zipline)
    alias(libs.plugins.redwood.generator.compose)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.kotlinSerialization)
}

redwoodSchema {
    source = project(":schema")
    type = "com.example.serverdrivenui.schema.SduiSchema"
}

kotlin {
    js {
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.redwood.compose)
            implementation(libs.redwood.widget)
            implementation(libs.redwood.treehouse)
            implementation(libs.redwood.treehouse.guest)
            implementation(libs.zipline)
            implementation(project(":shared"))
            implementation(libs.kotlinx.serialization.json)
            implementation(project(":core-data"))
        }
        val jsMain by getting {
            dependencies {
                implementation(project(":shared-protocol-guest"))
                implementation(project(":shared-widget"))
                implementation(npm("@js-joda/core", "3.2.0"))
            }
        }
    }
}
