package com.tneff.remotecomposeexample

import com.tneff.kmpremotecompose.remote.creation.RcExpression
import com.tneff.kmpremotecompose.remote.creation.RcPaint
import com.tneff.kmpremotecompose.remote.creation.ROOT_ALIGNMENT_CENTER
import com.tneff.kmpremotecompose.remote.creation.ROOT_SCALE_FILL_BOUNDS
import com.tneff.kmpremotecompose.remote.creation.ROOT_SCALE_FIT
import com.tneff.kmpremotecompose.remote.creation.ROOT_SCROLL_NONE
import com.tneff.kmpremotecompose.remote.creation.ROOT_SIZING_SCALE
import com.tneff.kmpremotecompose.remote.creation.addText
import com.tneff.kmpremotecompose.remote.creation.createTextFromFloat
import com.tneff.kmpremotecompose.remote.creation.document
import com.tneff.kmpremotecompose.remote.creation.drawOval
import com.tneff.kmpremotecompose.remote.creation.drawTextAnchored
import com.tneff.kmpremotecompose.remote.creation.floatExpression
import com.tneff.kmpremotecompose.remote.creation.paint
import com.tneff.kmpremotecompose.remote.creation.setRootContentBehavior
import com.tneff.kmpremotecompose.remote.player.core.RemoteContext
import com.tneff.kmpremotecompose.remote.wire.WireTypes
import org.jetbrains.compose.resources.ExperimentalResourceApi
import remotecomposeexample.shared.generated.resources.Res

/**
 * REM-108-example — the **Doc-Catalog** abstraction (TechSpec §3), the heart of the app: one list carries
 * everything and the Viewer is source-agnostic. Each [RcDocEntry] resolves to `.rc` bytes via [resolveBytes],
 * which is the only thing `RemoteComposeApp(loadRc = …)` is fed.
 */
enum class DocArea(val title: String, val tagId: String) {
    CREATION_DSL("Creation-DSL", "creation_dsl"),
    BUNDLED_RC("RC-Doc-Sprache", "bundled_rc"),
}

/** Where a doc's bytes come from — a DSL builder (A), a bundled `.rc` (B), or the REM-169 server (S5). */
sealed interface RcDocSource {
    /** (A) built in-app via the Creation-DSL — procedural `document{}` or Compose `captureSingleRemoteDocument`. */
    data class DslBuilder(val build: suspend () -> ByteArray) : RcDocSource

    /** (B) a pre-built `.rc` bundled as a Compose resource (`files/rc/<name>.rc`). */
    data class BundledRc(val resourcePath: String) : RcDocSource

    /** Server: the same doc over the REM-169 `GET /rc/{pageId}` (wired in S5; pageId == [RcDocEntry.id]). */
    data class ServerPage(val pageId: String, val params: Map<String, String> = emptyMap()) : RcDocSource
}

/** A catalogued doc: stable [id] (== server pageId for Area B; drives the `rc-docitem-{id}` testTag + rc-doc). */
data class RcDocEntry(val id: String, val title: String, val area: DocArea, val source: RcDocSource)

/** The single resolver `loadRc` is fed — loaded ONCE per Viewer (see ViewerScreen), not per frame. */
@OptIn(ExperimentalResourceApi::class)
suspend fun RcDocSource.resolveBytes(): ByteArray = when (this) {
    is RcDocSource.DslBuilder -> build()
    is RcDocSource.BundledRc -> Res.readBytes(resourcePath)
    // S5 (Load-from-Server): Ktor-client GET {base}/rc/{pageId}. Not wired yet — surfaced as a clear error.
    is RcDocSource.ServerPage -> error("server mode not wired yet (S5): pageId=$pageId")
}

// --- Area A — Creation-DSL builders ------------------------------------------------------------------
// `dsl_oval` is the real procedural builder (mirrors `:server` buildSimple2 / procedure_simple2 oracle). The
// other five are skeleton placeholders rendering the same oval for now (build-green + navigable + each has
// its own id so per-doc QA works); their distinct DSL surfaces (coreText / gradient / time-var clock /
// MODIFIER_CLICK+rcInteractive / captureSingleRemoteDocument) land in the Area-A builder-bodies follow-up.
private fun winW() = WireTypes.asNan(RemoteContext.ID_WINDOW_WIDTH)
private fun winH() = WireTypes.asNan(RemoteContext.ID_WINDOW_HEIGHT)

/** dsl_oval — a window-filling oval (mirrors :server buildSimple2 / procedure_simple2). */
private fun ovalDoc(): ByteArray = document(width = 300, height = 300, contentDescription = "Hello Oval") {
    setRootContentBehavior(ROOT_SCROLL_NONE, ROOT_ALIGNMENT_CENTER, ROOT_SIZING_SCALE, ROOT_SCALE_FIT)
    drawOval(left = 0f, top = 0f, right = winW(), bottom = winH())
}

/** dsl_text — centred styled text (mirrors the procedure_center_text1 text-anchor pattern, static label). */
private fun textDoc(): ByteArray = document(width = 300, height = 300, contentDescription = "Styled Text") {
    setRootContentBehavior(ROOT_SCROLL_NONE, ROOT_ALIGNMENT_CENTER, ROOT_SIZING_SCALE, ROOT_SCALE_FILL_BOUNDS)
    val cx = floatExpression(RcExpression.WINDOW_WIDTH, 0.5f, RcExpression.MUL)
    val cy = floatExpression(RcExpression.WINDOW_HEIGHT, 0.5f, RcExpression.MUL)
    paint { color(0xff1565c0.toInt()); textSize(40f) }
    val t = addText("RemoteCompose")
    drawTextAnchored(textId = t, x = cx, y = cy)
}

/** dsl_gradient — a linear-gradient-filled oval (mirrors the procedure_gradient1 paint block). */
private fun gradientDoc(): ByteArray = document(width = 300, height = 300, contentDescription = "Gradient Fill") {
    setRootContentBehavior(ROOT_SCROLL_NONE, ROOT_ALIGNMENT_CENTER, ROOT_SIZING_SCALE, ROOT_SCALE_FILL_BOUNDS)
    paint {
        linearGradient(
            x0 = 0f, y0 = 0f, x1 = 0f, y1 = RcExpression.WINDOW_HEIGHT,
            colors = intArrayOf(0xff00ff88.toInt(), 0xff0022ff.toInt()), stops = null, tile = RcPaint.TILE_REPEAT,
        )
    }
    drawOval(left = 0f, top = 0f, right = winW(), bottom = winH())
}

/** dsl_clock — a live-seconds counter (TIME_IN_SEC → text); time-driven, distinct from the static docs. */
private fun clockDoc(): ByteArray = document(width = 300, height = 300, contentDescription = "Clock Face") {
    setRootContentBehavior(ROOT_SCROLL_NONE, ROOT_ALIGNMENT_CENTER, ROOT_SIZING_SCALE, ROOT_SCALE_FILL_BOUNDS)
    val cx = floatExpression(RcExpression.WINDOW_WIDTH, 0.5f, RcExpression.MUL)
    val cy = floatExpression(RcExpression.WINDOW_HEIGHT, 0.5f, RcExpression.MUL)
    paint { color(0xff222222.toInt()); textSize(56f) }
    val secs = floatExpression(RcExpression.TIME_IN_SEC, 60.0f, RcExpression.MOD)
    val t = createTextFromFloat(value = secs, digitsBefore = 2, digitsAfter = 0, flags = 3)
    drawTextAnchored(textId = t, x = cx, y = cy)
}

private fun bundled(name: String) = RcDocSource.BundledRc("files/rc/$name.rc")
private fun dsl(build: suspend () -> ByteArray) = RcDocSource.DslBuilder(build)

/** The full catalog — Area A (6 DSL) then Area B (10 bundled). Order == list order (TechSpec §4). */
val exampleCatalog: List<RcDocEntry> = listOf(
    // Area A — Creation-DSL (TODO: distinct DSL surfaces per the title; placeholders render the oval for now).
    RcDocEntry("dsl_oval", "Hello Oval", DocArea.CREATION_DSL, dsl { ovalDoc() }),
    RcDocEntry("dsl_text", "Styled Text", DocArea.CREATION_DSL, dsl { textDoc() }),
    RcDocEntry("dsl_gradient", "Gradient Fill", DocArea.CREATION_DSL, dsl { gradientDoc() }),
    RcDocEntry("dsl_clock", "Clock Face", DocArea.CREATION_DSL, dsl { clockDoc() }),
    RcDocEntry("dsl_tap", "Tappable Counter", DocArea.CREATION_DSL, dsl { ovalDoc() }), // TODO MODIFIER_CLICK+rcInteractive
    RcDocEntry("dsl_compose_card", "Compose-DSL Card", DocArea.CREATION_DSL, dsl { ovalDoc() }), // TODO captureSingleRemoteDocument
    // Area B — Bundled `.rc` (ids == dev-2 server pageIds).
    RcDocEntry("rc_box", "Box", DocArea.BUNDLED_RC, bundled("c_box")),
    RcDocEntry("rc_text", "Text Baseline", DocArea.BUNDLED_RC, bundled("text_baseline")),
    RcDocEntry("rc_gradient", "Gradient", DocArea.BUNDLED_RC, bundled("procedure_gradient1")),
    RcDocEntry("rc_moon", "Moon Phases", DocArea.BUNDLED_RC, bundled("moon_phases")),
    RcDocEntry("rc_clock", "Clock", DocArea.BUNDLED_RC, bundled("clock_demo1_clock1")),
    RcDocEntry("rc_confetti", "Confetti", DocArea.BUNDLED_RC, bundled("impulse_demo_confetti_demo")),
    RcDocEntry("rc_scroll", "Vertical Scroll", DocArea.BUNDLED_RC, bundled("c_modifier_vertical_scroll")),
    RcDocEntry("rc_click", "On Click", DocArea.BUNDLED_RC, bundled("c_modifier_on_click")),
    RcDocEntry("rc_pie", "Pie Chart", DocArea.BUNDLED_RC, bundled("good_pie_chart")),
    RcDocEntry("rc_compass", "Compass (sensor)", DocArea.BUNDLED_RC, bundled("sensor_demo_compass")),
)

fun catalogFor(area: DocArea): List<RcDocEntry> = exampleCatalog.filter { it.area == area }
