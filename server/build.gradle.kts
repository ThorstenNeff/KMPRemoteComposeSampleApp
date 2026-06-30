/*
 * Copyright 2026 The KmpRemoteCompose Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// REM-173 — the SELF-CONTAINED sample `.rc` server. A thin JVM application that serves example docs over
// localhost-only HTTP. Depends ONLY on the PUBLISHED RemoteCompose library (com.tneff.kmpremotecompose:
// shared) — NOT on the lib's internal :server module — so the sample stays self-contained and the lib's
// :server stays a generic capability. The serving mechanics are a faithful port of the lib's REM-169
// (localhost-only / allowlist / ETag-304 / 404-400-no-leak) — the security-reviewed posture is preserved.
plugins {
    alias(libs.plugins.kotlinJvm)
    application
}

dependencies {
    // The published library (Creation-API `document{}` for procedural docs). Resolved from mavenLocal /
    // GitHub Packages (settings repos, com.tneff content-filtered). No project(":shared"), no :server-lib.
    implementation(libs.rc.shared)

    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.statusPages)

    testImplementation(libs.kotlin.testJunit)
    testImplementation(libs.junit)
    // In-process Ktor test harness (byte-anchor + contract via testApplication; no real socket).
    testImplementation(libs.ktor.server.testHost)
}

application {
    mainClass.set("com.tneff.kmpremotecompose.server.ServerMainKt")
}

// The published :shared pulls Compose/Skiko transitively (byte-irrelevant for the creation path); one
// lifecycle jar then resolves twice, so the application dist tasks would fail "Entry … is a duplicate".
// EXCLUDE picks the first copy (same artifact + version) — same fix the lib :server uses.
tasks.withType<AbstractCopyTask>().matching { it.name in setOf("installDist", "distZip", "distTar") }
    .configureEach { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }

// REM-173 — run the localhost example server. ./gradlew :server:runExampleServer -PserverPort=8080
tasks.register<JavaExec>("runExampleServer") {
    group = "application"
    description = "Run the localhost (127.0.0.1) example .rc server: GET /rc/{pageId}. -PserverPort=8080"
    mainClass.set("com.tneff.kmpremotecompose.server.ServerMainKt")
    classpath = sourceSets["main"].runtimeClasspath
    args = listOfNotNull(project.findProperty("serverPort") as String?)
}
