package io.sunland.chainpass.service

import com.typesafe.config.ConfigFactory
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.logging.*
import io.ktor.client.features.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.sunland.chainpass.common.WebSocket
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.exposedLogger
import org.slf4j.LoggerFactory

fun main() {
    val config = ConfigFactory.load()

    if (config.getBoolean("server.enable")) {
        embeddedServer(Netty, applicationEngineEnvironment {
            module {
                main()
            }

            connector {
                host = config.getString("server.host")
                port = config.getInt("server.port")
            }
        }).start()
    }

    if (config.getBoolean("service.enable")) {
        Database.connect(config.getConfig("database"))

        val httpClient = HttpClient(CIO) {
            install(WebSockets)
            install(Logging)

            defaultRequest {
                method = HttpMethod.Get
                host = config.getString("service.host")
                port = config.getInt("service.port")

                url {
                    protocol = URLProtocol.byName[config.getString("service.protocol")]!!
                }
            }
        }

        runBlocking {
            try {
                httpClient.webSocket(request = {
                    header("Socket-Type", WebSocket.Type.SERVICE)
                }) {
                    while (true) {
                        val frame = incoming.receive() as? Frame.Text ?: continue

                        val message = WebSocket.Message.from(frame)

                        Database.execute {
                            exposedLogger.info(message.text)
                        }

                        send(WebSocket.Message(message.text, WebSocket.Type.SERVICE).toFrame())
                    }
                }
            } catch (ex: Throwable) {
                LoggerFactory.getLogger(HttpClient::class.java).info(ex.message!!)
            }
        }
    }
}
