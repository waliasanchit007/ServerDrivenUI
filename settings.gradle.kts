rootProject.name = "ServerDrivenUI"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
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
            if (requested.id.id == "app.cash.redwood") {
                useModule("app.cash.redwood:redwood-gradle-plugin:${requested.version}")
            }
            if (requested.id.id == "app.cash.zipline") {
                useModule("app.cash.zipline:zipline-gradle-plugin:${requested.version}")
            }
        }
    }
}

dependencyResolutionManagement {
    repositories {
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
include(":androidApp")
include(":composeApp")
include(":shared")
include(":shared-protocol-host")
include(":shared-protocol-guest")
include(":schema")
include(":presenter")
include(":shared-widget")
include(":dev-server")
include(":admin-web")
include(":core-data")
