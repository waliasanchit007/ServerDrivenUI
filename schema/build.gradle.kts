plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.redwood)
}

kotlin {
    // jvm() // Not needed with kotlin("jvm") plugin
}

dependencies {
    implementation(libs.redwood.schema)
    implementation(project(":schema-types"))
}

redwoodSchema {
    type = "com.example.serverdrivenui.schema.SduiSchema"
}
