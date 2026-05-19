plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.zipline)
    // Required for @Serializable data classes used as wire types in
    // ZiplineService method signatures (e.g. List<Quote> return type
    // on HostQuotesProvider). Without this plugin Zipline's
    // .serializer() lookup fails at take<> time with
    // "Serializer for class 'X' is not found".
    alias(libs.plugins.kotlinSerialization)
    // KSP runs `konduit-treehouse-codegen` against
    // @KonduitAppService-annotated interfaces (currently just
    // SduiAppService) and emits the matching
    // `Generated<Name>Adapter`. Replaces the ~95-line
    // ManualSduiAppServiceAdapter.kt we used to hand-write.
    alias(libs.plugins.ksp)
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

// KSP wiring for the @KonduitAppService processor. For multiplatform
// projects, the metadata-config emits one generated file under
// commonMain that every target sees.
dependencies {
    add("kspCommonMainMetadata", libs.redwood.treehouse.codegen)
}

// Workaround for https://github.com/google/ksp/issues/1318 — every
// target's compileKotlin task must explicitly depend on the metadata
// KSP task so the generated source is materialized before per-target
// compilation walks it.
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>().all {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}

// Splice the generated sources into commonMain so jvm/js/iOS targets
// all resolve `GeneratedSduiAppServiceAdapter` from the same path.
kotlin.sourceSets.commonMain.configure {
    kotlin.srcDir("${layout.buildDirectory.get()}/generated/ksp/metadata/commonMain/kotlin")
}
