package io.sunland.chainpass.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.websocket.*
import io.sunland.chainpass.common.App
import io.sunland.chainpass.common.Screen
import io.sunland.chainpass.common.SettingsFactory
import io.sunland.chainpass.common.rememberAppState
import io.sunland.chainpass.common.view.ServerAddress

fun main() = application {
    val appState = rememberAppState(
        ServerAddress(),
        HttpClient(CIO) {
            install(WebSockets)
            install(Logging)
        },
        Screen.SERVER_CONNECTION
    )

    Window(title = "Chain Pass", onCloseRequest = {
        appState.httpClientState.value.close()

        exitApplication()
    }) { App(SettingsFactory("${System.getProperty("user.home")}/.chain-pass"), appState) }
}