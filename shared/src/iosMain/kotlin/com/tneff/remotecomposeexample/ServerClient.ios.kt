package com.tneff.remotecomposeexample

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin

/** The iOS simulator shares the host network, so the REM-173 server's `127.0.0.1` is reachable as-is. */
actual val rcServerHost: String = "127.0.0.1"

actual fun newRcHttpClient(): HttpClient = HttpClient(Darwin)
