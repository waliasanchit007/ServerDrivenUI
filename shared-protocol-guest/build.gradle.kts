plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.redwood.generator.protocol.guest)
}

redwoodSchema {
    source = project(":schema")
    type = "com.example.serverdrivenui.schema.SduiSchema"
}

kotlin {
    js {
        browser()
        // binaries.executable() // Removed
        // Actually libs usually don't need executable(), but if I want to run tests maybe.
        // I'll keep it simple: js(IR) { browser() }
    }
    
    // Also support JVM if needed for testing?
    // Let's match presenter target which is JS.
    
    sourceSets {
        commonMain.dependencies {
            implementation(libs.redwood.protocol.guest)
            implementation(project(":shared-widget"))
        }
    }
}
