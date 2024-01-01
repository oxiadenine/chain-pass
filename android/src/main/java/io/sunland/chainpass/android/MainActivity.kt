package io.sunland.chainpass.android

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.sunland.chainpass.common.*

class MainActivity : AppCompatActivity() {
    private lateinit var appState: AppState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val settingsManager = SettingsManager(applicationContext.getExternalFilesDir("")!!.absolutePath)

        setContent {
            title = "Chain Pass"

            appState = rememberAppState(
                Settings(),
                Storage(settingsManager.dirPath),
                HttpClient {
                    install(WebSockets)
                },
                Screen.SERVER_CONNECTION
            )

            App(settingsManager, appState)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        appState.httpClientState.value.close()
    }
}