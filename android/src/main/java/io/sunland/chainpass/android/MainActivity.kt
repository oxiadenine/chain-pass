package io.sunland.chainpass.android

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.websocket.*
import io.sunland.chainpass.common.*

class MainActivity : AppCompatActivity() {
    private lateinit var appState: AppState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            title = "Chain Pass"

            appState = rememberAppState(
                Settings(),
                HttpClient(CIO) {
                    install(WebSockets)
                    install(Logging)
                },
                Screen.SERVER_CONNECTION
            )

            App(SettingsManager(applicationContext.getExternalFilesDir("")!!.absolutePath), appState)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        appState.httpClientState.value.close()
    }
}