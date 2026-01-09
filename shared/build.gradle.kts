plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.zipline)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    jvm()
    jvm()
    js(IR) {
        browser()
        binaries.library()
    }
    iosArm64()
    iosSimulatorArm64()
    
    sourceSets {
        commonMain.dependencies {
            api(project(":shared-widget")) // Widget interfaces
            implementation(libs.redwood.treehouse)
            implementation(libs.redwoodProtocol)
            implementation(libs.redwood.protocol.host)
            implementation(libs.redwood.protocol.host)
            api(libs.zipline)
            
        }
    }
}
