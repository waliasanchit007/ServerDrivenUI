plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.redwood.generator.widget)
}

redwoodSchema {
    source = project(":schema")
    type = "com.example.serverdrivenui.schema.SduiSchema"
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
            api(libs.redwood.widget)
        }
    }
}
