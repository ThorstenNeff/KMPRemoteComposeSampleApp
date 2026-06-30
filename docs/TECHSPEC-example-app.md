# TechSpec — RemoteComposeExample (Beispiel-App, konsumiert die KmpRemoteCompose-Lib)

> **Autor:** PO-Assistent (Reviewer) · **Status:** Draft v1 · **Scope:** Mensch-Anforderung — eine
> Beispiel-/Template-App (Android + iOS), die die KmpRemoteCompose-Lib **konsumiert**. **Kein §2-Byte-Concern**
> — die App rendert/baut nur, sie verändert das Format nicht. Pragmatisch halten. **dev-1 baut die App, dev-2
> den Server gegen diese Spec.** Output bewusst nach `RemoteComposeExample/docs/` (eigenes Projekt; dev-1/dev-2
> lokal → Filesystem ist der Share-Pfad). ⚠️ `RemoteComposeExample` ist **noch kein git-Repo** — `git init`
> empfehlenswert für die App-Entwicklung (PO-Hinweis).

---

## 0. Kernziel (TL;DR)

Hauptmenü → **2 Bereiche**: **(A) Creation-DSL-Docs** (in-App via `document{}`/`captureSingleRemoteDocument{}`
gebaut) und **(B) Bundled-`.rc`-Docs** (kuratierter Korpus-Subset). Bereich → **Doc-Liste** → Tap →
**Viewer** rendert via `RemoteComposeApp(loadRc = { bytes })`. Plus ein **„Load from Server"-Modus** (REM-169-
Server, separat als Prozess; App = HTTP-**Client**). **Theme** aus `./elf-schemes` (Material3 light/dark) →
Compose `ColorScheme`, system-gesteuert. Die ganze App hängt an **einer Abstraktion**: dem **Doc-Katalog**
(`{id, title, area, source}`), wo `source` ∈ {DSL-Builder, Bundled-`.rc`, Server-Page}.

---

## 1. Ist-Stand (`RemoteComposeExample` Skeleton)

- KMP: `:androidApp` + `:shared` (commonMain/androidMain/iosMain), `iosApp` = Xcode-Projekt, konsumiert das
  `Shared`-Framework (iosArm64/iosSimulatorArm64, static). Versions-Catalog (`libs.versions.toml`),
  typesafe-project-accessors. **Versionen passen exakt zur Lib** (Kotlin 2.4.0 / Compose 1.11.1 / AGP 9.0.1 /
  compileSdk 36) — Pflicht für ABI-Kompatibilität.
- `:shared` commonMain hat schon Compose (runtime/foundation/material3/ui/components.resources) + lifecycle-
  viewmodel/runtime-compose. **Die Lib ist noch NICHT verdrahtet** (§5).

**Lib-Konsum-API (aus den Reviews, was die App nutzt):**
- `@Composable RemoteComposeApp(loadRc: suspend (String) -> ByteArray, …, callbacks: RcInteractionCallbacks =
  NoOp, modifier)` — der Render-Eintritt. `loadRc` liefert die Bytes (egal woher).
- `document(width, height, profile = Profile.Baseline, contentDescription?, content): ByteArray` (prozedural,
  sync) und `suspend captureSingleRemoteDocument(…): ByteArray` (Compose-Creation-DSL) — die Byte-Produzenten.
- `startLocalRcServer(port, registry): LocalRcServer` + `RcPageRegistry` + `GET /rc/{pageId}` (REM-169) — der
  **separate** Dev/Local-Server (JVM/Netty).
- `RcInteractionCallbacks` (onClick/onScroll) + `Modifier.rcInteractive(callbacks)` (REM-108) — optionale
  Interaktions-Senke (für die interaktiven Demo-Docs).

---

## 2. Navigations-Modell (state-based, pragmatisch — keine Nav-Library)

`menu → area → list → viewer`, als **sealed Screen-State** (kein nav-Framework nötig, hält's klein + KMP-portabel):
```kotlin
sealed interface Screen {
    data object Menu : Screen
    data class DocList(val area: DocArea) : Screen
    data class Viewer(val entry: RcDocEntry, val fromServer: Boolean) : Screen
}
enum class DocArea(val title: String) { CREATION_DSL("Creation-DSL"), BUNDLED_RC("RC-Doc-Sprache") }
```
- Ein `var screen by rememberSaveable(stateSaver = …) { mutableStateOf(Screen.Menu) }` im Root-Composable
  (oder ein winziger `AppNavState`-Holder im ViewModel). **Back:** Viewer→List→Menu (Android-Back-Handler +
  iOS-Top-Bar-Back). Kein Deep-Linking nötig (MVP).
- Screens: **MenuScreen** (2 große Cards/Buttons → Bereich), **DocListScreen(area)** (`LazyColumn` der Einträge
  des Bereichs + ein **„Load from Server"-Toggle**), **ViewerScreen(entry)** (`RemoteComposeApp` + Top-Bar-Titel
  + Back).

---

## 3. Doc-Katalog-Abstraktion (das Herzstück)

Eine **einzige Liste** trägt alles; der Viewer ist quell-agnostisch:
```kotlin
data class RcDocEntry(val id: String, val title: String, val area: DocArea, val source: RcDocSource)

sealed interface RcDocSource {
    /** (A) in-App gebaut via Creation-DSL — prozedural ODER Compose-DSL. */
    data class DslBuilder(val build: suspend () -> ByteArray) : RcDocSource
    /** (B) vorgefertigtes .rc, als Compose-Resource gebündelt (files/rc/<name>.rc). */
    data class BundledRc(val resourcePath: String) : RcDocSource
    /** Server: derselbe Doc über REM-169 `GET /rc/{pageId}?params`. */
    data class ServerPage(val pageId: String, val params: Map<String, String> = emptyMap()) : RcDocSource
}
```
**Bytes-Resolver** (das Einzige, was `loadRc` füttert) — der Viewer ruft pro Frame nichts neu; lädt **einmal**:
```kotlin
suspend fun RcDocSource.resolveBytes(http: RcHttp, base: ServerBaseUrl): ByteArray = when (this) {
    is DslBuilder -> build()                                  // document{}/capture{} → bytes
    is BundledRc  -> Res.readBytes(resourcePath)             // Compose-Resources
    is ServerPage -> http.getRcBytes(base, pageId, params)   // Ktor-Client GET (§6)
}
// Viewer: RemoteComposeApp(loadRc = { resolved })  — resolved EINMAL geladen (remember/LaunchedEffect).
```
- **Server-Modus-Toggle:** in `DocListScreen` schaltet ein Switch jeden Eintrag von seiner nativen `source` auf
  die `ServerPage`-Variante (sofern der Server diesen `pageId` registriert hat — §6). So zeigt **derselbe** Doc
  drei Lade-Pfade (DSL / bundled / server) — genau die Lib-Demo-Absicht.

---

## 4. Korpus-Doc-Kuratierung (Vielfalt = die Lib zeigen)

**Bereich A — Creation-DSL (in-Code-Builder, ~6; zeigt prozedural UND Compose-DSL):**
| id | Titel | DSL-Surface | Kategorie |
|---|---|---|---|
| `dsl_oval` | Hello Oval | `document{ drawOval(…) }` (prozedural, wie `:server buildOval`) | Shapes |
| `dsl_text` | Styled Text | `document{ … coreText/drawTextRun … }` | Text |
| `dsl_gradient` | Gradient Fill | `document{ … procedural gradient … }` | Color |
| `dsl_clock` | Clock Face | `document{ … time-var-gesteuerte Arcs/Lines … }` | Animation/Time |
| `dsl_tap` | Tappable Counter | `document{ … MODIFIER_CLICK + VALUE_INTEGER_CHANGE … }` (+ `rcInteractive`) | Interaktion |
| `dsl_compose_card` | Compose-DSL Card | `captureSingleRemoteDocument{ RemoteCoreText(…)/… }` | Compose-DSL-Pfad |

> Builder-Bodies baut dev-1 (mirror der Korpus-Orakel-Muster / `:server`-Beispiele). Die Spec fixiert nur
> Katalog + Intent + welche DSL-Oberfläche jeder exerziert. `dsl_compose_card` MUSS `captureSingleRemoteDocument`
> nutzen (zeigt den Compose-Creation-Pfad neben dem prozeduralen).

**Bereich B — Bundled `.rc` (Korpus-Subset, ~10; max. Vielfalt):**
| id | `.rc` | Kategorie |
|---|---|---|
| `rc_box` | `c_box.rc` | Basic Shapes |
| `rc_text` | `text_baseline.rc` | Text |
| `rc_gradient` | `procedure_gradient1.rc` | Gradient |
| `rc_moon` | `moon_phases.rc` | Path/Clip-Geometrie |
| `rc_clock` | `clock_demo1_clock1.rc` | Animierte Uhr |
| `rc_confetti` | `impulse_demo_confetti_demo.rc` | Particles (touch-getriggert) |
| `rc_scroll` | `c_modifier_vertical_scroll.rc` | Interaktion (Scroll) |
| `rc_click` | `c_modifier_on_click.rc` | Interaktion (Click) |
| `rc_pie` | `good_pie_chart.rc` | Daten/Chart |
| `rc_compass` | `sensor_demo_compass.rc` | Sensor (capability-gestaffelt: statischer Frame ohne Sensor) |

> Die 10 `.rc` werden aus `KmpRemoteCompose/.../rc-corpus/corpus/` in die App als **Compose-Resources**
> (`composeResources/files/rc/<name>.rc`) **kopiert** (Build-Schritt oder einmaliges Einchecken). Die Server-
> Page-Registry (§6) registriert dieselben ids → so deckt der Server-Modus Bereich B (und optional A) ab.

---

## 5. Lib-Wiring (Versionen exakt — Pflicht)

**Empfehlung: `mavenLocal()` als Default** (entspricht dem REM-168-consumer-smoke-Muster, kein Token nötig,
realistischer External-Consumer-Pfad):
1. Lib publishen: `./gradlew :shared:publishToMavenLocal :creation-compose:publishToMavenLocal` (KmpRemoteCompose).
2. App `dependencyResolutionManagement { repositories { mavenLocal(); google(); mavenCentral() } }` + `:shared`-deps:
   `implementation("com.tneff.kmpremotecompose:shared:0.1.0")` (+ `:creation-compose` falls die Compose-DSL-Builder
   genutzt werden — `dsl_compose_card`). Compose-Deps bringt die App selbst (Lib exposed Compose als `implementation`).
- **GHP-Pfad (dokumentiert, external-team-real):** GitHub-Packages-Repo (REM-168) mit `read:packages`-Token NUR
  aus `~/.gradle/gradle.properties`/env (`gpr.user`/`gpr.token`) — **nie committet**. Auskommentiert im Skeleton,
  Opt-in.
- **`includeBuild`-Option (Dev-Convenience):** `includeBuild("../KmpRemoteCompose")` = Composite-Build gegen den
  Lib-Source, immer frisch, kein Publish-Schritt — schnellste Iteration während der Lib-Entwicklung. Als
  dokumentierte Dev-Alternative; **Default bleibt mavenLocal** (template-repräsentativ).
- **Versions-Lock:** Kotlin 2.4.0 / Compose 1.11.1 / AGP 9.0.1 / compileSdk 36 — schon im Skeleton-Catalog, exakt
  = Lib. **Drift hier = ABI/Compose-Compiler-Bruch** → nicht abweichen.

---

## 6. Server-Integration (App = HTTP-Client zu separatem REM-169-Server)

🔴 **Wichtiger Architektur-Punkt:** REM-169 `startLocalRcServer` nutzt Ktor-**Netty** = **JVM-only** → läuft
NICHT eingebettet im iOS-App-Prozess (Kotlin/Native). Daher:
- **Der Server ist ein SEPARATER Prozess** (dev-2: das `:server`-Modul / `startLocalRcServer`, auf der Dev-
  Maschine gestartet), **NICHT in die Mobile-App eingebettet.** Das deckt sich mit REM-169s Dev/Local-Posture
  (localhost-gebunden, dev-gated).
- **Die App ist HTTP-Client** — **Ktor-Client** (multiplatform: OkHttp/Android, Darwin/iOS) holt
  `GET {base}/rc/{pageId}?params` → Bytes → `RemoteComposeApp(loadRc)`. (Neue App-Dep: `ktor-client-core` +
  per-Target-Engine. Das ist die App-Dep, nicht die Lib.)
- **`ServerBaseUrl` (konfigurierbar, plattform-localhost-Nuance!):**
  - iOS-Simulator: `http://127.0.0.1:<port>` (teilt host-loopback).
  - Android-Emulator: **`http://10.0.2.2:<port>`** (Emulator-Alias für host-localhost — NICHT 127.0.0.1).
  - Realgerät: die **LAN-IP der Dev-Maschine** (`http://<dev-ip>:<port>`), Server muss dann erreichbar sein —
    das berührt REM-169s **localhost-only-Posture** → **wenn ein Realgerät-Demo gebraucht wird, ist das der
    human-gated Exposed-Schritt (REM-170)**, nicht in dieser Spec. MVP-Default: Emulator/Simulator.
  - Default-Port + Base-URL als App-Setting (einfaches Config-Objekt; kein UI-Zwang).
- **Server-Page-Registry (dev-2):** ein `defaultExampleRegistry()` registriert die Bereich-B-ids (+ optional die
  A-DSL-Builder als server-seitige Pages) → `pageId == RcDocEntry.id`. Fail-closed 404 (REM-169) für unbekannte.
- **Load-from-Server-Modus:** der Toggle (§3) leitet `resolveBytes` auf `ServerPage`. Fällt der Server aus
  (nicht gestartet / unerreichbar) → **klare Fehler-UI** („Server nicht erreichbar unter {base}") statt Crash
  (fail-soft; der native DSL/bundled-Pfad bleibt der Default).

---

## 7. Theme-Übersetzung (`./elf-schemes` → Compose `ColorScheme`, system-gesteuert)

- `elf-schemes/material3-light-theme.css` + `material3-dark-theme.css` tragen **Material3-`--md-sys-color-*`-
  Tokens** (primary, on-primary, primary-container, secondary, tertiary, error, background, surface,
  surface-container-*, outline, inverse-*, …). **1:1-Map** auf Compose `lightColorScheme(...)` /
  `darkColorScheme(...)`-Parameter; CSS-Hex `#355CA8` → `Color(0xFF355CA8)`.
- **Übersetzung (einmalig, statisch):** dev-1 übersetzt die ~30 Tokens je Scheme in zwei `ColorScheme`-Konstanten
  (`RcExampleLightColors` / `RcExampleDarkColors`) — **kein Runtime-CSS-Parsing** (die CSS ist die Quelle, die
  Konstanten sind der Code; ein kleiner Gen-Schritt oder Hand-Map, ~30 Zeilen). Mapping-Tabelle: `--md-sys-color-
  primary → primary`, `…-on-primary → onPrimary`, `…-primary-container → primaryContainer`, `…-surface-container-
  high → surfaceContainerHigh`, etc. (Compose-M3-`ColorScheme`-Feldnamen = camelCase der Token).
- **System-gesteuert:** `MaterialTheme(colorScheme = if (isSystemInDarkTheme()) RcExampleDarkColors else
  RcExampleLightColors) { … }` im Root. Folgt der OS-Einstellung automatisch.
- Die **gerenderten `.rc`-Docs** sind davon unberührt (sie tragen ihre eigenen Farben); das Theme stylt nur die
  **App-Chrome** (Menü/Listen/Top-Bar/Viewer-Rahmen).

---

## 8. Observability & Testbarkeit (test-1 — per-Doc-Maestro-gateable, Skeleton-Teil)

Erstklassig im Skeleton, **nicht Nachtrag** — damit das Maestro-Gate **pro Doc** sauber wird (statt nur
Varianz-Check). §2-irrelevant (App-UI/render-only; berührt kein `.rc`-Byte).

**(1) Viewer-Observability-Kontrakt (wie die Haupt-App, REM-8/REM-83):**
- **`rc-rendered`** — gesetzt **erst nach dem ersten committeten Frame mit `drawCount > 0`** (kein false-green
  vor einem echten Paint). Der Viewer liest den honest-render-Gate vom Player (`RemoteContext.drawCount`) wie
  `RemoteComposeApp` — die App reicht das an einen sichtbaren/test-adressierbaren Marker durch.
- **`rc-doc`** — der **`RcDocEntry.id` des gerade gerenderten Docs** (stabil, nicht der Titel). An den
  committeten Frame gebunden (nie drift gegen `rc-rendered` — gleicher one-settling-recompose wie in der
  Haupt-App). Ermöglicht „welcher Doc rendert gerade" per-Doc-Assertions.
- **`rc-error`** — optional, fail-soft: leerer Render (`drawCount==0`) oder Server-unerreichbar → adressierbarer
  Fehler-Marker statt blank-false-green (deckt sich mit §6 fail-soft-UI).
- Diese Marker sind **render-only Test-Surface** (kein `.rc`/wire-Touch); auf Android via `testTag` +
  `testTagsAsResourceId` (unten), iOS via Accessibility-Identifier.

**(2) testTag-Konvention (stabile, sprach-unabhängige Tags — NICHT die lokalisierten Titel):**
| Element | testTag | Zweck |
|---|---|---|
| Root/Scaffold | `rc-app-root` | App-Bereitschaft |
| Main-Menu-Container | `rc-menu` | Menu-Screen da |
| Area-Eintrag (Menu) | `rc-area-{areaId}` (`rc-area-creation_dsl` / `rc-area-bundled_rc`) | Bereich-Tap |
| Doc-Liste | `rc-doclist` | Listen-Screen da |
| Listen-Item | `rc-docitem-{entryId}` (z.B. `rc-docitem-rc_confetti`) | per-Doc-Tap-Ziel (stabil über `id`) |
| Server-Toggle | `rc-server-toggle` | Lade-Pfad-Umschalter |
| Viewer-Canvas | `rc-viewer` (+ die `rc-rendered`/`rc-doc`/`rc-error`-Marker darin/daneben) | Render-Assertion |
| Back | `rc-back` | Navigation |

- **Android:** `Modifier.semantics { testTagsAsResourceId = true }` am Root (einmal), damit `testTag` als
  resource-id für UiAutomator/Maestro sichtbar wird. **iOS:** dieselben Strings als Accessibility-Identifier.
- **Tag-Stabilität:** Tags leiten sich aus `DocArea`-/`RcDocEntry.id` ab (ASCII, lower_snake), **nie** aus dem
  lokalisierten/Theme-abhängigen Titel → Maestro-Flows brechen nicht bei Text-/Theme-Änderung.
- **Per-Doc-Maestro-Muster (das der Kontrakt ermöglicht):** `tap rc-area-bundled_rc → tap rc-docitem-{id} →
  assert rc-doc == {id} && rc-rendered present` — pro Doc, pro Lade-Pfad (Toggle), pro Target.

---

## 9. Slice-Zerlegung (dev-1 App · dev-2 Server)

| Slice | Owner | Inhalt |
|---|---|---|
| **S1 Lib-Wiring + 1 Doc end-to-end + Observability-Skeleton** | dev-1 | mavenLocal-Wiring (§5) + `RemoteComposeApp(loadRc={Res.readBytes(...)})` rendert **ein** bundled `.rc` sichtbar (Android + iOS). **Inkl. die `rc-rendered`/`rc-doc`-Viewer-Marker + `testTagsAsResourceId`-Root (§8) ab S1** (Foundation, nicht nachgerüstet). |
| **S2 Katalog + Navigation + beide Bereiche** | dev-1 | `RcDocEntry`/`RcDocSource`/`resolveBytes` (§3); Menu→List→Viewer (§2); Bereich A (6 DSL-Builder, §4) + Bereich B (10 bundled, §4). |
| **S3 Theme** | dev-1 | elf-schemes → `ColorScheme`-Konstanten (§7), system-gesteuert. |
| **S4 Server-Run + Registry** | dev-2 | `:server`-Runner mit `defaultExampleRegistry()` (Bereich-B-ids als Pages, §6); lokal startbar (REM-169 startLocalRcServer, localhost). |
| **S5 Load-from-Server-Modus** | dev-1 (+dev-2-Contract) | Ktor-Client + `ServerBaseUrl` (plattform-localhost, §6) + Toggle + fail-soft-Fehler-UI. |
| **D1 (deferred, human-gated) Realgerät-über-LAN** | — | berührt REM-169-Exposition (REM-170) → Mensch. MVP = Emulator/Simulator. |

**Parallel:** dev-1 S1→S2→S3→S5 (App); dev-2 S4 (Server-Registry) gegen den `RcDocEntry.id == pageId`-Contract,
nutzbar ab S2. S5 braucht S4s Registry-ids.

## 10. Verifikation (pragmatisch — Beispiel-App, kein §2)

- **„Done means proven", jetzt per-Doc-gateable (§8):** Maestro-Flow pro Doc/Bereich/Lade-Pfad:
  `tap rc-area-{area} → tap rc-docitem-{id} → assert rc-doc=={id} && rc-rendered present` auf Android **und** iOS.
  Server-Modus: mit gestartetem `:server` denselben Flow über den `rc-server-toggle`. Der `rc-rendered`-honest-
  gate (drawCount>0) verhindert blank-false-green.
- Kein Byte-/Conformance-Gate (die App konsumiert nur; Byte-Korrektheit ist Lib-Sache).

## 11. Offene Punkte (PO/Mensch)

- **Q1 (Lib-Wiring-Default):** mavenLocal (Empfehlung) vs includeBuild (schneller für Lib-Mitentwicklung). —
  *reversibel, dev-Wahl.*
- **Q2 (🔴 Server-auf-Realgerät):** über LAN erreichbar = REM-169-Exposition → **human-gated REM-170.** MVP nur
  Emulator/Simulator (localhost/10.0.2.2). Realgerät-Demo = Mensch-Entscheid. — *Risk-Posture → Mensch.*
- **Q3 (Korpus-`.rc`-Bundling):** Build-Schritt (Copy aus KmpRemoteCompose-corpus) vs einmal-einchecken. —
  *reversibel; Copy-Task bevorzugt (single source of truth).*
- **Q4 (`git init` für RemoteComposeExample):** kein git-Repo aktuell → für App-Dev empfohlen (sonst kein
  Branch/Review/Share-via-git). — *PO/Mensch.*
