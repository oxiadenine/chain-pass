package io.sunland.chainpass.desktop

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.rsocket.kotlin.core.WellKnownMimeType
import io.rsocket.kotlin.keepalive.KeepAlive
import io.rsocket.kotlin.ktor.client.RSocketSupport
import io.rsocket.kotlin.payload.PayloadMimeType
import io.sunland.chainpass.common.App
import io.sunland.chainpass.common.SettingsManager
import kotlin.time.Duration.Companion.seconds

fun main() = application {
    val httpClient = HttpClient {
        install(WebSockets)
        install(RSocketSupport) {
            connector {
                connectionConfig {
                    keepAlive = KeepAlive(
                        interval = 30.seconds,
                        maxLifetime = 30.seconds
                    )
                    payloadMimeType = PayloadMimeType(
                        data = WellKnownMimeType.ApplicationJson,
                        metadata = WellKnownMimeType.MessageRSocketCompositeMetadata
                    )
                }
            }
        }
    }

    val settingsManager = SettingsManager("${System.getProperty("user.home")}/.chain-pass")

    Window(
        icon = painterResource("icon.png"),
        title = "Chain Pass",
        onCloseRequest = {
            httpClient.close()

            exitApplication()
        }
    ) { App(httpClient, settingsManager) }
}