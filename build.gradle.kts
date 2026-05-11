plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.redwood) apply false
    alias(libs.plugins.redwood.generator.compose) apply false
    alias(libs.plugins.redwood.generator.widget) apply false
    alias(libs.plugins.redwood.generator.protocol.host) apply false
    alias(libs.plugins.redwood.generator.protocol.guest) apply false
    alias(libs.plugins.redwood.generator.modifiers) apply false
    alias(libs.plugins.zipline) apply false
}

// Phase 5b dev loop entry point — see docs/KONDUIT_PLAN.md.
//
// `./gradlew konduitDev` exec's bin/konduit-dev which orchestrates the
// continuous guest compile, the dev-server, and (if a device is
// connected) a logcat tail. Gradle isn't great at long-running parallel
// processes — the bash script handles cleanup of background pids on
// EXIT/INT/TERM. Both invocations are equivalent:
//
//   ./gradlew konduitDev
//   bin/konduit-dev
//
// The Gradle wrapper exists so users following the canonical "do
// everything via ./gradlew" pattern don't have to discover the script.
tasks.register<Exec>("konduitDev") {
    group = "Konduit"
    description = "Run the full Konduit dev loop: continuous guest compile + dev-server + (optional) logcat tail."
    workingDir = rootDir
    commandLine("bin/konduit-dev")
    standardInput = System.`in`
}