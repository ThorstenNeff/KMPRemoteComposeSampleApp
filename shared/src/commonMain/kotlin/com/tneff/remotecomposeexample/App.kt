package com.tneff.remotecomposeexample

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

/**
 * REM-108-example — the app root: a `MaterialTheme` (S3 swaps in the elf-schemes ColorScheme) over the
 * state-based [Screen] navigation (TechSpec §2). `rc-app-root` is the readiness testTag; `testTagsAsResourceId`
 * for Maestro is enabled at the Android entry (MainActivity) / iOS accessibility ids.
 *
 * Phase-0's single-doc smoke is now [ViewerScreen]; the catalog ([exampleCatalog]) drives both areas.
 */
@Composable
fun App(modifier: Modifier = Modifier) {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) RcExampleDarkColors else RcExampleLightColors) {
        Surface(modifier.fillMaxSize().testTag("rc-app-root")) {
            var screen by remember { mutableStateOf<Screen>(Screen.Menu) }
            when (val s = screen) {
                Screen.Menu -> MenuScreen(
                    onArea = { area -> screen = Screen.DocList(area) },
                )
                is Screen.DocList -> DocListScreen(
                    area = s.area,
                    onEntry = { entry, fromServer -> screen = Screen.Viewer(entry, fromServer) },
                    onBack = { screen = Screen.Menu },
                )
                is Screen.Viewer -> ViewerScreen(
                    entry = s.entry,
                    fromServer = s.fromServer,
                    onBack = { screen = Screen.DocList(s.entry.area) },
                )
            }
        }
    }
}
