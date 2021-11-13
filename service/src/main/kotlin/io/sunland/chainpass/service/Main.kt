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
import io.sunland.chainpass.common.network.SocketMessage
import io.sunland.chainpass.common.network.SocketMessageType
import io.sunland.chainpass.common.network.SocketConnectionType
import io.sunland.chainpass.common.repository.ChainEntity
import io.sunland.chainpass.common.repository.ChainLinkEntity
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

                header("Socket-Type", SocketConnectionType.SERVICE.name)
            }
        }

        runBlocking {
            val logger = LoggerFactory.getLogger(HttpClient::class.java)

            httpClient.webSocket {
                while (true) {
                    val frame = runCatching { incoming.receive() }.fold(
                        onSuccess = { frame -> frame as Frame.Text },
                        onFailure = { exception ->
                            logger.info(exception.message)

                            return@webSocket
                        }
                    )

                    val fromMessage = SocketMessage.from(frame)

                    when (fromMessage.type) {
                        SocketMessageType.CHAIN_CREATE -> {
                            val chainEntity = Json.decodeFromString<ChainEntity>(fromMessage.text)

                            ChainDataRepository.create(chainEntity).map { id ->
                                chainEntity.id = id

                                SocketMessage(SocketMessageType.CHAIN_CREATE, Json.encodeToString(chainEntity))
                            }
                        }
                        SocketMessageType.CHAIN_READ -> {
                            ChainDataRepository.read().map { chains ->
                                SocketMessage(SocketMessageType.CHAIN_READ, Json.encodeToString(chains))
                            }
                        }
                        SocketMessageType.CHAIN_DELETE -> {
                            val chainEntity = Json.decodeFromString<ChainEntity>(fromMessage.text)

                            ChainDataRepository.delete(chainEntity)
                        }
                        SocketMessageType.CHAIN_LINK_CREATE -> {
                            val chainLinkEntity = Json.decodeFromString<ChainLinkEntity>(fromMessage.text)

                            ChainLinkDataRepository.create(chainLinkEntity).map { chainLinkId ->
                                chainLinkEntity.id = chainLinkId

                                SocketMessage(SocketMessageType.CHAIN_LINK_CREATE, Json.encodeToString(chainLinkEntity))
                            }
                        }
                        SocketMessageType.CHAIN_LINK_READ -> {
                            val chainEntity = Json.decodeFromString<ChainEntity>(fromMessage.text)

                            ChainLinkDataRepository.read(chainEntity).map { chainLinks ->
                                SocketMessage(SocketMessageType.CHAIN_LINK_READ, Json.encodeToString(chainLinks))
                            }
                        }
                        SocketMessageType.CHAIN_LINK_UPDATE -> {
                            val chainLinkEntity = Json.decodeFromString<ChainLinkEntity>(fromMessage.text)

                            ChainLinkDataRepository.update(chainLinkEntity)
                        }
                        SocketMessageType.CHAIN_LINK_DELETE -> {
                            val chainLinkEntity = Json.decodeFromString<ChainLinkEntity>(fromMessage.text)

                            ChainLinkDataRepository.delete(chainLinkEntity)
                        }
                    }.fold(
                        onSuccess = { message ->
                            if (message is SocketMessage) {
                                send(message.toFrame(fromMessage.socketId))
                            }
                        },
                        onFailure = { exception -> logger.info(exception.message) }
                    )
                }
            }

            httpClient.close()
        }
    }
}
