package io.sunland.chainpass.desktop

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.websocket.*
import io.sunland.chainpass.common.*

fun main() = application {
    val appState = rememberAppState(
        Settings(),
        HttpClient(CIO) {
            install(WebSockets)
            install(Logging)
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
    ) { App(SettingsManager("${System.getProperty("user.home")}/.chain-pass"), appState) }
}