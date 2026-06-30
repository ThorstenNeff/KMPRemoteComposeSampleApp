plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    // REM-173 — kotlin("jvm") for the :server module; declared here (apply false) so it's on the
    // buildscript classpath at the known version (avoids the "already on classpath, unknown version" clash).
    alias(libs.plugins.kotlinJvm) apply false
}