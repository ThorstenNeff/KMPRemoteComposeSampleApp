package com.tneff.remotecomposeexample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.tneff.kmpremotecompose.RcRouter
import com.tneff.kmpremotecompose.RemoteComposeApp

/** REM-108-example — state-based navigation (TechSpec §2): `Menu → DocList(area) → Viewer(entry)`. */
sealed interface Screen {
    data object Menu : Screen
    data class DocList(val area: DocArea) : Screen
    data class Viewer(val entry: RcDocEntry, val fromServer: Boolean) : Screen
}

/** Main menu — two area cards. testTags: `rc-menu`, `rc-area-{areaId}`. */
@Composable
fun MenuScreen(onArea: (DocArea) -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp).testTag("rc-menu"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("RemoteCompose Example", style = MaterialTheme.typography.headlineSmall)
        DocArea.entries.forEach { area ->
            Card(
                onClick = { onArea(area) },
                modifier = Modifier.fillMaxWidth().testTag("rc-area-${area.tagId}"),
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(area.title, style = MaterialTheme.typography.titleLarge)
                    Text("${catalogFor(area).size} docs", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

/** Doc list for an area + the (S5) server toggle. testTags: `rc-doclist`, `rc-docitem-{id}`, `rc-server-toggle`. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocListScreen(area: DocArea, onEntry: (RcDocEntry, Boolean) -> Unit, onBack: () -> Unit) {
    var fromServer by remember { mutableStateOf(false) }
    Scaffold(topBar = { TopAppBar(title = { Text(area.title) }, navigationIcon = { BackButton(onBack) }) }) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().testTag("rc-doclist")) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Text("Load from server", modifier = Modifier.padding(end = 12.dp))
                Switch(checked = fromServer, onCheckedChange = { fromServer = it }, modifier = Modifier.testTag("rc-server-toggle"))
            }
            LazyColumn(Modifier.fillMaxSize()) {
                items(catalogFor(area), key = { it.id }) { entry ->
                    Card(
                        onClick = { onEntry(entry, fromServer) },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp).testTag("rc-docitem-${entry.id}"),
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(entry.title, style = MaterialTheme.typography.titleMedium)
                            Text(entry.id, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Viewer — renders the entry via [RemoteComposeApp]. [RcRouter.select] aligns the library's `rc-doc` hook to
 * the entry id (test-1's per-doc assert), while `loadRc` resolves the actual bytes (source-agnostic). The
 * library already surfaces the honest `rc-rendered` (drawCount>0) gate. testTags: `rc-viewer`, `rc-back`,
 * `rc-live-toggle`.
 *
 * **Live toggle (REM-172):** flips [RcRouter.live], which the library's [RemoteComposeApp] reads to opt the
 * render loop into advancing frame-time (clocks tick, confetti animates) AND to wire the live pointer
 * gestures (REM-108) — so `dsl_tap`/`rc_click`/`rc_scroll` become interactive. Default **off** = the static
 * deterministic frame (the Maestro render gate stays static); the user opts in for the live demo. Reset to
 * off when leaving the viewer so the global flag never leaks into the next doc / the gate.
 *
 * **Server mode (REM-172 S5):** when `fromServer`, the bytes come from `ServerPage(entry.id)` over dev-2's
 * REM-173 localhost server (else the native [RcDocEntry.source]). Bytes are pre-resolved in a [produceState]
 * with a `try/catch` so a server-down / 404 is **fail-soft** — the `rc-error` UI shows instead of crashing.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewerScreen(entry: RcDocEntry, fromServer: Boolean, onBack: () -> Unit) {
    var live by remember { mutableStateOf(false) }
    LaunchedEffect(entry.id) { RcRouter.select(entry.id) }
    LaunchedEffect(live) { RcRouter.live = live }
    DisposableEffect(Unit) { onDispose { RcRouter.live = false } }
    val source = if (fromServer) RcDocSource.ServerPage(entry.id) else entry.source
    val load by produceState<DocLoad>(DocLoad.Loading, entry.id, fromServer) {
        value = try {
            DocLoad.Ready(source.resolveBytes())
        } catch (t: Throwable) {
            DocLoad.Failed(t.message ?: t::class.simpleName ?: "load failed")
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(entry.title) },
                navigationIcon = { BackButton(onBack) },
                actions = {
                    if (fromServer) Text("⤓", modifier = Modifier.padding(end = 8.dp).testTag("rc-from-server"))
                    Text("Live", modifier = Modifier.padding(end = 4.dp))
                    Switch(checked = live, onCheckedChange = { live = it }, modifier = Modifier.testTag("rc-live-toggle"))
                },
            )
        },
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize().testTag("rc-viewer")) {
            when (val s = load) {
                is DocLoad.Loading -> Text("Loading…", modifier = Modifier.padding(24.dp).testTag("rc-loading"))
                is DocLoad.Failed -> Column(Modifier.padding(24.dp).testTag("rc-error")) {
                    Text("Couldn't load from server", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Is the local server running? (./gradlew :server:runExampleServer)\n${s.message}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                is DocLoad.Ready -> RemoteComposeApp(loadRc = { _ -> s.bytes })
            }
        }
    }
}

/** Viewer load state — server (or native) bytes are pre-resolved so a failure is fail-soft, not a crash. */
private sealed interface DocLoad {
    data object Loading : DocLoad
    data class Ready(val bytes: ByteArray) : DocLoad
    data class Failed(val message: String) : DocLoad
}

@Composable
private fun BackButton(onBack: () -> Unit) {
    TextButton(onClick = onBack, modifier = Modifier.testTag("rc-back")) { Text("‹ Back") }
}
