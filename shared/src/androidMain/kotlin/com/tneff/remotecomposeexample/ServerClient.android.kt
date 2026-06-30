package com.tneff.remotecomposeexample

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

/** Android emulator reaches the host machine (where the REM-173 server binds 127.0.0.1) at `10.0.2.2`. */
actual val rcServerHost: String = "10.0.2.2"

actual fun newRcHttpClient(): HttpClient = HttpClient(OkHttp)
