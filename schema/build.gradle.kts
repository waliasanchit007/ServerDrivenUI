plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.redwood)
}

redwoodSchema {
    type = "com.example.serverdrivenui.schema.SduiSchema"
}

dependencies {
    implementation(libs.redwood.schema)
}
