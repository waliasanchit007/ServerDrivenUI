rootProject.name = "ServerDrivenUI"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        mavenLocal()
        // Konduit private fork — see konduit-read.md. Auth via gpr.user /
        // gpr.token gradle props OR GITHUB_ACTOR / GITHUB_TOKEN env vars
        // (the latter is what the CI workflow uses).
        maven {
            url = uri("https://maven.pkg.github.com/waliasanchit007/konduit")
            credentials {
                username = (providers.gradleProperty("gpr.user").orNull
                    ?: System.getenv("GITHUB_ACTOR")).orEmpty()
                password = (providers.gradleProperty("gpr.token").orNull
                    ?: System.getenv("GITHUB_TOKEN")).orEmpty()
            }
        }
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "app.cash.zipline") {
                useModule("app.cash.zipline:zipline-gradle-plugin:${requested.version}")
            }
        }
    }
}

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        // Konduit private fork — see CI auth note above.
        maven {
            url = uri("https://maven.pkg.github.com/waliasanchit007/konduit")
            credentials {
                username = (providers.gradleProperty("gpr.user").orNull
                    ?: System.getenv("GITHUB_ACTOR")).orEmpty()
                password = (providers.gradleProperty("gpr.token").orNull
                    ?: System.getenv("GITHUB_TOKEN")).orEmpty()
            }
        }
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

include(":composeApp")
include(":androidApp")
include(":schema")
include(":schema-types")
include(":presenter")
include(":shared")
include(":shared-protocol-host")
include(":shared-protocol-guest")
include(":shared-widget")
include(":shared-modifier")
include(":dev-server")
