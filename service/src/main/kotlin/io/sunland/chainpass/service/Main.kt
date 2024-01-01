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
import io.sunland.chainpass.common.repository.Chain
import io.sunland.chainpass.common.repository.ChainLink
import io.sunland.chainpass.service.repository.ChainLinkDataRepository
import io.sunland.chainpass.service.repository.ChainDataRepository
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
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
                    header("Socket-Type", WebSocket.ConnectionType.SERVICE)
                }) {
                    while (true) {
                        val frame = incoming.receive() as? Frame.Text ?: continue

                        val message = WebSocket.Message.from(frame)

                        when (message.type) {
                            WebSocket.MessageType.CREATE_CHAIN -> {
                                val chain = Json.decodeFromString<Chain>(message.text)

                                chain.id = ChainDataRepository.create(chain)

                                send(WebSocket.Message(
                                    Json.encodeToString(chain),
                                    WebSocket.MessageType.CREATE_CHAIN).toFrame()
                                )
                            }
                            WebSocket.MessageType.READ_CHAIN -> {
                                val chains = ChainDataRepository.read()

                                send(WebSocket.Message(
                                    Json.encodeToString(chains),
                                    WebSocket.MessageType.READ_CHAIN).toFrame()
                                )
                            }
                            WebSocket.MessageType.DELETE_CHAIN -> {
                                val chain = Json.decodeFromString<Chain>(message.text)

                                ChainDataRepository.delete(chain)
                            }
                            WebSocket.MessageType.CREATE_CHAIN_LINK -> {
                                val chainLink = Json.decodeFromString<ChainLink>(message.text)

                                chainLink.id = ChainLinkDataRepository.create(chainLink)

                                send(WebSocket.Message(
                                    Json.encodeToString(chainLink),
                                    WebSocket.MessageType.CREATE_CHAIN_LINK).toFrame()
                                )
                            }
                            WebSocket.MessageType.READ_CHAIN_LINK -> {
                                val chain = Json.decodeFromString<Chain>(message.text)

                                val chainLinks = ChainLinkDataRepository.read(chain)

                                send(WebSocket.Message(
                                    Json.encodeToString(chainLinks),
                                    WebSocket.MessageType.READ_CHAIN_LINK).toFrame()
                                )
                            }
                            WebSocket.MessageType.UPDATE_CHAIN_LINK -> {
                                val chainLink = Json.decodeFromString<ChainLink>(message.text)

                                ChainLinkDataRepository.update(chainLink)
                            }
                            WebSocket.MessageType.DELETE_CHAIN_LINK -> {
                                val chainLink = Json.decodeFromString<ChainLink>(message.text)

                                ChainLinkDataRepository.delete(chainLink)
                            }
                            else -> exposedLogger.info(message.text)
                        }
                    }
                }
            } catch (ex: Throwable) {
                LoggerFactory.getLogger(HttpClient::class.java).info(ex.message!!)
            }
        }
    }
}
