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
import io.sunland.chainpass.common.SocketMessage
import io.sunland.chainpass.common.SocketMessageType
import io.sunland.chainpass.common.SocketType
import io.sunland.chainpass.common.repository.Chain
import io.sunland.chainpass.common.repository.ChainLink
import io.sunland.chainpass.common.socketId
import io.sunland.chainpass.service.repository.ChainLinkDataRepository
import io.sunland.chainpass.service.repository.ChainDataRepository
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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

                header("Socket-Type", SocketType.SERVICE)
                header("Socket-Id", socketId())
            }
        }

        runBlocking {
            try {
                httpClient.webSocket {
                    while (true) {
                        val frame = incoming.receive() as? Frame.Text ?: continue

                        val fromMessage = SocketMessage.from(frame)

                        val toMessage: SocketMessage? = when (fromMessage.type) {
                            SocketMessageType.CREATE_CHAIN -> {
                                val chain = Json.decodeFromString<Chain>(fromMessage.text)

                                chain.id = ChainDataRepository.create(chain)

                                SocketMessage(SocketMessageType.CREATE_CHAIN, fromMessage.id, Json.encodeToString(chain))
                            }
                            SocketMessageType.READ_CHAIN -> {
                                val chains = ChainDataRepository.read()

                                SocketMessage(SocketMessageType.READ_CHAIN, fromMessage.id, Json.encodeToString(chains))
                            }
                            SocketMessageType.DELETE_CHAIN -> {
                                val chain = Json.decodeFromString<Chain>(fromMessage.text)

                                ChainDataRepository.delete(chain)

                                null
                            }
                            SocketMessageType.CREATE_CHAIN_LINK -> {
                                val chainLink = Json.decodeFromString<ChainLink>(fromMessage.text)

                                chainLink.id = ChainLinkDataRepository.create(chainLink)

                                SocketMessage(SocketMessageType.CREATE_CHAIN_LINK, fromMessage.id, Json.encodeToString(chainLink))
                            }
                            SocketMessageType.READ_CHAIN_LINK -> {
                                val chain = Json.decodeFromString<Chain>(fromMessage.text)

                                val chainLinks = ChainLinkDataRepository.read(chain)

                                SocketMessage(SocketMessageType.READ_CHAIN_LINK, fromMessage.id, Json.encodeToString(chainLinks))
                            }
                            SocketMessageType.UPDATE_CHAIN_LINK -> {
                                val chainLink = Json.decodeFromString<ChainLink>(fromMessage.text)

                                ChainLinkDataRepository.update(chainLink)

                                null
                            }
                            SocketMessageType.DELETE_CHAIN_LINK -> {
                                val chainLink = Json.decodeFromString<ChainLink>(fromMessage.text)

                                ChainLinkDataRepository.delete(chainLink)

                                null
                            }
                        }

                        toMessage?.run { send(toMessage.toFrame()) }
                    }
                }
            } catch (ex: Throwable) {
                LoggerFactory.getLogger(HttpClient::class.java).info(ex.message!!)
            }
        }
    }
}
