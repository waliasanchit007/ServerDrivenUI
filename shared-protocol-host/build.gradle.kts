plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.redwood.generator.protocol.host)
}

redwoodSchema {
    source = files("../schema/src/commonMain/resources")
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
    }
}
