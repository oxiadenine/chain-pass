package io.sunland.chainpass.android

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import com.typesafe.config.ConfigFactory
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.logging.*
import io.ktor.client.features.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.sunland.chainpass.common.App
import io.sunland.chainpass.common.SocketConnectionType
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val environment = if (BuildConfig.DEBUG) "development" else "production"

        val config = ConfigFactory.load("application.$environment").getConfig("client")

        val httpClient = HttpClient(CIO) {
            install(WebSockets)
            install(Logging)

            defaultRequest {
                method = HttpMethod.Get
                host = config.getString("host")
                port = config.getInt("port")

                url {
                    protocol = URLProtocol.byName[config.getString("protocol")]!!
                }

                header("Socket-Type", SocketConnectionType.CLIENT)
                header("Socket-Id", UUID.randomUUID().toString())
            }
        }

        setContent {
            title = "Chain Pass"

            MaterialTheme {
                App(httpClient)
            }
        }
    }
}
