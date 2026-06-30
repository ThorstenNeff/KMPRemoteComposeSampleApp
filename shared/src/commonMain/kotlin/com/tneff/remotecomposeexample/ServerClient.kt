package com.tneff.remotecomposeexample

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.readRawBytes

/**
 * REM-172 **S5 — "load from server"** client for dev-2's REM-173 localhost `.rc` server
 * (`GET /rc/{pageId}`, `application/octet-stream`). The 10 Area-B `rc_*` catalog ids are the server's
 * pageIds 1:1, so `ServerPage(entry.id)` maps directly.
 *
 * Host is **per-platform** (the local server binds `127.0.0.1`): the Android emulator reaches the host
 * machine at `10.0.2.2`, the iOS simulator shares the host network at `127.0.0.1` — hence [rcServerHost]
 * is `expect`/`actual`. The engine is per-platform too (OkHttp / Darwin) via [newRcHttpClient].
 *
 * [loadFromServer] throws on server-down / 404 (no special-casing here) — the Viewer catches it and shows
 * the fail-soft `rc-error` UI instead of crashing.
 */
expect val rcServerHost: String

/** Platform Ktor engine factory — OkHttp on Android, Darwin on iOS. */
expect fun newRcHttpClient(): HttpClient

object RcServer {
    /** dev-2's REM-173 default port (`./gradlew :server:runExampleServer`, default 8080). */
    const val PORT: Int = 8080

    private val client: HttpClient by lazy { newRcHttpClient() }

    /** `GET http://{host}:{PORT}/rc/{pageId}` → raw `.rc` bytes. Throws on connect-failure/404 (Viewer fail-soft). */
    suspend fun loadFromServer(pageId: String): ByteArray =
        client.get("http://$rcServerHost:$PORT/rc/$pageId").readRawBytes()
}
