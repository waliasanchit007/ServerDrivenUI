plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.zipline)
    // Required for @Serializable data classes used as wire types in
    // ZiplineService method signatures (e.g. List<Quote> return type
    // on HostQuotesProvider). Without this plugin Zipline's
    // .serializer() lookup fails at take<> time with
    // "Serializer for class 'X' is not found".
    alias(libs.plugins.kotlinSerialization)
}

// U11 lint — rejects ZiplineService methods with function-typed
// parameters. Used to be a copy/pasted ~50-line inline task; now
// a one-line application thanks to konduit-gradle-plugin's
// dev.konduit.zipline-shapes (caliclan.4+). See Konduit
// docs/KNOWN_BUGS.md U11.
//
// Why `apply` not `plugins { alias(...) }`: every dev.konduit.* plugin
// ships from the same konduit-gradle-plugin jar, so when any of them
// (e.g. dev.konduit.schema, applied transitively through redwoodSchema)
// brings the jar onto the buildscript classpath, the plugins{} block
// trips a Gradle version-disambiguation check
// ("this plugin is already on the classpath with an unknown version").
// The `apply` form bypasses that check — the plugin class is found,
// instantiated, and Plugin#apply is called.
apply(plugin = "dev.konduit.zipline-shapes")

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
            api(libs.zipline)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
