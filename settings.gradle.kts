rootProject.name = "RemoteComposeExample"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
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
}

dependencyResolutionManagement {
    repositories {
        // REM-108-example P0: the RemoteCompose library (com.tneff.kmpremotecompose:shared). Preferred
        // source is the GitHub Packages repo, but it needs a PAT (read:packages) the build env doesn't carry
        // — so P0 consumes the locally-published 0.1.0 from mavenLocal (PO-sanctioned fallback; the lib's
        // own publish puts :shared:0.1.0 there). To switch to GHP, add the maven block below + creds in
        // ~/.gradle/gradle.properties (gpr.user / gpr.key) and drop mavenLocal.
        mavenLocal {
            mavenContent { includeGroupAndSubgroups("com.tneff") }
        }
        // maven {
        //     url = uri("https://maven.pkg.github.com/ThorstenNeff/kmpremotecompose")
        //     credentials {
        //         username = providers.gradleProperty("gpr.user").orNull ?: System.getenv("GITHUB_ACTOR")
        //         password = providers.gradleProperty("gpr.key").orNull ?: System.getenv("GITHUB_TOKEN")
        //     }
        //     mavenContent { includeGroupAndSubgroups("com.tneff") }
        // }
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

include(":androidApp")
include(":shared")
include(":server")