# Local example `.rc` server (REM-173)

A **self-contained** localhost server (`:server`) that serves the sample's example `.rc` documents over
HTTP, for the app's "load from server" mode. It depends only on the **published** RemoteCompose library
(`com.tneff.kmpremotecompose:shared`) + Ktor — no dependency on the library's internal `:server`.

**Android + iOS only; localhost-only** — public exposure is human-gated (REM-170) and not included; there
is no CORS/web. The serving mechanics (allowlist / ETag-304 / 404-400-no-leak / 127.0.0.1 bind) are a
faithful port of the library's security-reviewed REM-169 HTTP layer.

## Prerequisite

The published library must be resolvable (the sample consumes `com.tneff:*` from `mavenLocal` by default;
GitHub Packages is the alternative — see `settings.gradle.kts`). From the **library** repo:

```bash
./gradlew :shared:publishToMavenLocal
```

## Run

```bash
./gradlew :server:runExampleServer            # default port 8080
./gradlew :server:runExampleServer -PserverPort=9000
```

Binds **`127.0.0.1` only**, prints its URL + pageIds. `GET /rc/{pageId}` →
`application/octet-stream` + strong ETag (→ `304` on `If-None-Match`); `GET /rc` lists pageIds; unknown
id → `404`. Ctrl-C stops it (clean shutdown).

## pageId → doc map

The 10 **catalog (Area-B)** ids match the app's catalog **verbatim**, served as the bundled lib-corpus
`.rc` **verbatim** (so the app's server-toggle `ServerPage(rc_*)` maps 1:1):

| pageId | bundled `.rc` |
|---|---|
| `rc_box` | `c_box.rc` |
| `rc_text` | `text_baseline.rc` |
| `rc_gradient` | `procedure_gradient1.rc` |
| `rc_moon` | `moon_phases.rc` |
| `rc_clock` | `clock_demo1_clock1.rc` |
| `rc_confetti` | `impulse_demo_confetti_demo.rc` |
| `rc_scroll` | `c_modifier_vertical_scroll.rc` |
| `rc_click` | `c_modifier_on_click.rc` |
| `rc_pie` | `good_pie_chart.rc` |
| `rc_compass` | `sensor_demo_compass.rc` |

Plus procedural `document{}` extras: `simple2`, `oval`, `circle`. `pageId` is an **allowlist key**, never
a filesystem path.

## App integration ("load from server")

**Host per platform:** Android emulator reaches the host at `10.0.2.2`; the iOS simulator shares the host
network at `127.0.0.1`.

```kotlin
// RC_SERVER_HOST: Android emulator = "10.0.2.2", iOS simulator = "127.0.0.1"
// port: the one the server printed (default 8080)
suspend fun loadFromServer(pageId: String): ByteArray =
    httpClient.get("http://$RC_SERVER_HOST:$port/rc/$pageId").readRawBytes()

// then:  RemoteComposeApp(loadRc = ::loadFromServer)   — fail-soft if the server is down
```

`httpClient` is a Ktor `HttpClient` (any engine). Only an HTTP client is needed app-side — no `:server`
or JVM dependency in the mobile app.
