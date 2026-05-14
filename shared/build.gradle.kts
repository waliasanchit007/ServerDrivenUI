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
