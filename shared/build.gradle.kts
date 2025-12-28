plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.zipline)
}

kotlin {
    jvm()
    js {
        browser()
    }
    iosArm64()
    iosSimulatorArm64()
    
    sourceSets {
        commonMain.dependencies {
            api(project(":shared-widget")) // Widget interfaces
            implementation(libs.redwood.treehouse)
            implementation(libs.redwoodProtocol)
            implementation(libs.redwood.protocol.host)
            implementation(libs.zipline)
        }
    }
}
