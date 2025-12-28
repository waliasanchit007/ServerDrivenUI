plugins {
    alias(libs.plugins.kotlinJvm)
    application
}

application {
    mainClass.set("com.example.serverdrivenui.devserver.DevServerKt")
}

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.websockets)
    implementation(libs.ktor.server.host.common)
    implementation("ch.qos.logback:logback-classic:1.4.14")
}
