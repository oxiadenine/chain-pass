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

                        val toMessage = when (fromMessage.type) {
                            SocketMessageType.CHAIN_CREATE -> {
                                val chain = Json.decodeFromString<Chain>(fromMessage.text)

                                ChainDataRepository.create(chain).map { chainId ->
                                    chain.id = chainId

                                    SocketMessage(
                                        SocketMessageType.CHAIN_CREATE,
                                        fromMessage.id,
                                        Json.encodeToString(chain)
                                    )
                                }
                            }
                            SocketMessageType.CHAIN_READ -> {
                                ChainDataRepository.read().map { chains ->
                                    SocketMessage(
                                        SocketMessageType.CHAIN_READ,
                                        fromMessage.id,
                                        Json.encodeToString(chains)
                                    )
                                }
                            }
                            SocketMessageType.CHAIN_DELETE -> {
                                val chain = Json.decodeFromString<Chain>(fromMessage.text)

                                ChainDataRepository.delete(chain)
                            }
                            SocketMessageType.CHAIN_LINK_CREATE -> {
                                val chainLink = Json.decodeFromString<ChainLink>(fromMessage.text)

                                ChainLinkDataRepository.create(chainLink).map { chainLinkId ->
                                    chainLink.id = chainLinkId

                                    SocketMessage(
                                        SocketMessageType.CHAIN_LINK_CREATE,
                                        fromMessage.id,
                                        Json.encodeToString(chainLink)
                                    )
                                }
                            }
                            SocketMessageType.CHAIN_LINK_READ -> {
                                val chain = Json.decodeFromString<Chain>(fromMessage.text)

                                ChainLinkDataRepository.read(chain).map { chainLinks ->
                                    SocketMessage(
                                        SocketMessageType.CHAIN_LINK_READ,
                                        fromMessage.id,
                                        Json.encodeToString(chainLinks)
                                    )
                                }
                            }
                            SocketMessageType.CHAIN_LINK_UPDATE -> {
                                val chainLink = Json.decodeFromString<ChainLink>(fromMessage.text)

                                ChainLinkDataRepository.update(chainLink)
                            }
                            SocketMessageType.CHAIN_LINK_DELETE -> {
                                val chainLink = Json.decodeFromString<ChainLink>(fromMessage.text)

                                ChainLinkDataRepository.delete(chainLink)
                            }
                        }.getOrThrow()

                        if (toMessage is SocketMessage) {
                            send(toMessage.toFrame())
                        }
                    }
                }
            } catch (ex: Throwable) {
                LoggerFactory.getLogger(HttpClient::class.java).info(ex.message!!)
            }
        }
    }
}
