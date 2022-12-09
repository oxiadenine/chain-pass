package io.sunland.chainpass.desktop

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.sunland.chainpass.common.*

fun main() = application {
    val settingsManager = SettingsManager("${System.getProperty("user.home")}/.chain-pass")

    val appState = rememberAppState(
        Settings(),
        Storage(settingsManager.dirPath),
        HttpClient {
            install(WebSockets)
        },
        Screen.SERVER_CONNECTION
    )

    Window(
        icon = painterResource("icon.png"),
        title = "Chain Pass",
        onCloseRequest = {
            appState.httpClientState.value.close()

            exitApplication()
        }
    ) { App(settingsManager, appState) }
}