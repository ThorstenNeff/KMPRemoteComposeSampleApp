import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
    
    androidLibrary {
       namespace = "com.tneff.remotecomposeexample.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
    
       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            // S5 (REM-172/REM-173): Ktor client OkHttp engine for the Android server-mode load path.
            implementation("io.ktor:ktor-client-okhttp:3.1.3")
        }
        iosMain.dependencies {
            // S5: Ktor client Darwin engine for the iOS server-mode load path.
            implementation("io.ktor:ktor-client-darwin:3.1.3")
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            // REM-108-example P0: the RemoteCompose player/creation library (mavenLocal 0.1.0). Versions
            // match exactly (CMP 1.11.1 / Kotlin 2.4.0) so no resolution conflicts. Provides RemoteComposeApp.
            implementation("com.tneff.kmpremotecompose:shared:0.1.0")
            // Phase-1 Area-A: the Compose-Creation DSL (captureSingleRemoteDocument + Remote* composables) for
            // the dsl_compose_card entry — the Compose-creation path alongside the procedural document{}.
            implementation("com.tneff.kmpremotecompose:creation-compose:0.1.0")
            // S5 (REM-172/REM-173): Ktor client core (commonMain) for the "load from server" path; engine is
            // per-platform (okhttp Android / darwin iOS). NB pinned to 3.1.3, NOT the server's 3.2.0: client
            // and server Ktor versions are independent (HTTP contract), and 3.2.0's client jar has a
            // space-in-SimpleName class (io.ktor.client.plugins.Messages) that needs DEX 040 (minSdk 30) —
            // it fails D8 dexing at our locked minSdk 24. 3.1.3 is the pre-regression client.
            implementation("io.ktor:ktor-client-core:3.1.3")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}