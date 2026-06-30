package com.tneff.remotecomposeexample

import androidx.compose.runtime.Composable
import com.tneff.kmpremotecompose.RemoteComposeApp
import org.jetbrains.compose.resources.ExperimentalResourceApi
import remotecomposeexample.shared.generated.resources.Res

/**
 * REM-108-example **Phase 0 — Lib-Wiring-Smoke.** Renders ONE bundled corpus doc (`procedure_simple1.rc`,
 * copied verbatim from the library corpus into this app's `composeResources/files/rc/`) through the
 * library's [RemoteComposeApp], proving the consumption path end-to-end — the `:shared:0.1.0` dependency
 * resolves and the player renders — on Android + iOS.
 *
 * The `loadRc` lambda ignores the requested name and always returns the smoke doc; Phase 1 replaces this
 * with the real per-area doc catalog + navigation (assist TechSpec). [RemoteComposeApp] already exposes the
 * `rc-rendered` / `rc-doc` render hooks test-1's Maestro gate needs (render-only, §2-safe).
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
fun App() {
    RemoteComposeApp(
        loadRc = { _ -> Res.readBytes("files/rc/procedure_simple1.rc") },
    )
}
