plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.redwood)
}

kotlin {
    // jvm() // Not needed with kotlin("jvm") plugin
}

dependencies {
    implementation(libs.redwood.schema)
}

redwoodSchema {
    type = "com.example.serverdrivenui.schema.SduiSchema"
}
