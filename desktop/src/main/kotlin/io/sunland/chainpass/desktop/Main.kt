package io.sunland.chainpass.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.logging.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.sunland.chainpass.common.*
import io.sunland.chainpass.common.view.ServerAddress

fun main() = application {
    val appState = rememberAppState(
        ServerAddress(),
        HttpClient(CIO) {
            install(WebSockets)
            install(Logging)

            defaultRequest { method = HttpMethod.Get }
        },
        Screen.SERVER_CONNECTION
    )

    Window(title = "Chain Pass", onCloseRequest = {
        appState.httpClientState.value.close()

        exitApplication()
    }) { App(SettingsFactory("${System.getProperty("user.home")}/.chain-pass"), appState) }
}
