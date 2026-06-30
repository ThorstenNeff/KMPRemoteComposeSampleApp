package com.tneff.remotecomposeexample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            // REM-108-example (test-1 req): expose Compose testTags as UiAutomator/Maestro resource-ids so
            // the per-doc Maestro gate (rc-area-*, rc-docitem-*, rc-doc, rc-rendered, rc-back) is addressable.
            App(modifier = Modifier.semantics { testTagsAsResourceId = true })
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
