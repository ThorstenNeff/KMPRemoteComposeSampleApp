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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
 * library already surfaces the honest `rc-rendered` (drawCount>0) gate. testTags: `rc-viewer`, `rc-back`.
 * NB: `fromServer` is carried but resolves to the native source until S5 wires the Ktor client (fail-soft).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewerScreen(entry: RcDocEntry, onBack: () -> Unit) {
    LaunchedEffect(entry.id) { RcRouter.select(entry.id) }
    Scaffold(topBar = { TopAppBar(title = { Text(entry.title) }, navigationIcon = { BackButton(onBack) }) }) { pad ->
        Box(Modifier.padding(pad).fillMaxSize().testTag("rc-viewer")) {
            RemoteComposeApp(loadRc = { _ -> entry.source.resolveBytes() })
        }
    }
}

@Composable
private fun BackButton(onBack: () -> Unit) {
    TextButton(onClick = onBack, modifier = Modifier.testTag("rc-back")) { Text("‹ Back") }
}
