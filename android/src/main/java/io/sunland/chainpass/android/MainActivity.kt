package io.sunland.chainpass.android

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.rsocket.kotlin.core.WellKnownMimeType
import io.rsocket.kotlin.keepalive.KeepAlive
import io.rsocket.kotlin.ktor.client.RSocketSupport
import io.rsocket.kotlin.payload.PayloadMimeType
import io.sunland.chainpass.common.*
import kotlin.time.Duration.Companion.seconds

class MainActivity : AppCompatActivity() {
    private lateinit var httpClient: HttpClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val settingsManager = SettingsManager(applicationContext.getExternalFilesDir("")!!.absolutePath)

        httpClient = HttpClient {
            install(WebSockets)
            install(RSocketSupport) {
                connector {
                    maxFragmentSize = 1024

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

        setContent {
            title = "Chain Pass"

            val appState = rememberAppState(Settings(), Screen.SERVER_CONNECTION)

            App(settingsManager, httpClient, appState)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        httpClient.close()
    }
}