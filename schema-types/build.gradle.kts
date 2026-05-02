plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    jvm()
    js {
        browser()
    }
    iosArm64()
    iosSimulatorArm64()
}
