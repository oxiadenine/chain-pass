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
import io.sunland.chainpass.common.Chain
import io.sunland.chainpass.common.network.SocketMessage
import io.sunland.chainpass.common.network.SocketMessageType
import io.sunland.chainpass.common.network.SocketConnectionType
import io.sunland.chainpass.common.repository.ChainEntity
import io.sunland.chainpass.common.repository.ChainKeyEntity
import io.sunland.chainpass.common.repository.ChainLinkEntity
import io.sunland.chainpass.common.security.PasswordEncoder
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
                    runCatching {
                        val frame = incoming.receive() as Frame.Text

                        val fromMessage = SocketMessage.from(frame)

                        val toMessage = when (fromMessage.type) {
                            SocketMessageType.CHAIN_CREATE -> {
                                val chainEntity = Json.decodeFromString<ChainEntity>(fromMessage.data.getOrThrow())

                                val chain = Chain().apply {
                                    id = chainEntity.id
                                    name = Chain.Name(chainEntity.name)
                                    key = Chain.Key(PasswordEncoder.hash(chainEntity.key, chainEntity.name))
                                }

                                val chainEntityHashKey = ChainEntity(chain.id, chain.name.value, chain.key.value)

                                ChainDataRepository.create(chainEntityHashKey).fold(
                                    onSuccess = { chainEntityId ->
                                        chainEntity.id = chainEntityId

                                        SocketMessage.success(SocketMessageType.CHAIN_CREATE, Json.encodeToString(chainEntity))
                                    },
                                    onFailure = { exception ->
                                        SocketMessage.failure(SocketMessageType.CHAIN_CREATE, exception.message!!)
                                    }
                                )
                            }
                            SocketMessageType.CHAIN_READ -> {
                                ChainDataRepository.read().fold(
                                    onSuccess = { chainEntities ->
                                        val chainEntitiesNoKey = chainEntities.map { chainEntity ->
                                            Chain().apply {
                                                id = chainEntity.id
                                                name = Chain.Name(chainEntity.name)
                                            }
                                        }.map { chain -> ChainEntity(chain.id, chain.name.value, chain.key.value) }

                                        SocketMessage.success(SocketMessageType.CHAIN_READ, Json.encodeToString(chainEntitiesNoKey))
                                    },
                                    onFailure = { exception ->
                                        SocketMessage.failure(SocketMessageType.CHAIN_READ, exception.message!!)
                                    }
                                )
                            }
                            SocketMessageType.CHAIN_DELETE -> {
                                val chainKeyEntity = Json.decodeFromString<ChainKeyEntity>(fromMessage.data.getOrThrow())

                                ChainDataRepository.read(chainKeyEntity.id)
                                    .mapCatching { chainEntity ->
                                        val chainKey = Chain.Key(PasswordEncoder.hash(chainKeyEntity.key, chainEntity.name))

                                        Chain.Key(chainEntity.key).matches(chainKey.value)
                                    }
                                    .mapCatching { ChainDataRepository.delete(chainKeyEntity).getOrThrow() }
                                    .fold(
                                        onSuccess = {
                                            SocketMessage.success(SocketMessageType.CHAIN_DELETE)
                                        },
                                        onFailure = { exception ->
                                            SocketMessage.failure(SocketMessageType.CHAIN_DELETE, exception.message!!)
                                        }
                                    )
                            }
                            SocketMessageType.CHAIN_LINK_CREATE -> {
                                val chainLinkEntity = Json.decodeFromString<ChainLinkEntity>(fromMessage.data.getOrThrow())

                                 ChainDataRepository.read(chainLinkEntity.chainKey.id)
                                     .mapCatching { chainEntity ->
                                         val chainKey = Chain.Key(PasswordEncoder.hash(chainLinkEntity.chainKey.key, chainEntity.name))

                                         Chain.Key(chainEntity.key).matches(chainKey.value)
                                     }
                                     .mapCatching { ChainLinkDataRepository.create(chainLinkEntity).getOrThrow() }
                                     .fold(
                                         onSuccess = { chainLinkId ->
                                             chainLinkEntity.id = chainLinkId

                                             SocketMessage.success(SocketMessageType.CHAIN_LINK_CREATE, Json.encodeToString(chainLinkEntity))
                                         },
                                         onFailure = { exception ->
                                             SocketMessage.failure(SocketMessageType.CHAIN_LINK_CREATE, exception.message!!)
                                         }
                                    )
                            }
                            SocketMessageType.CHAIN_LINK_READ -> {
                                val chainKeyEntity = Json.decodeFromString<ChainKeyEntity>(fromMessage.data.getOrThrow())

                                ChainDataRepository.read(chainKeyEntity.id)
                                    .mapCatching { chainEntity ->
                                        val chainKey = Chain.Key(PasswordEncoder.hash(chainKeyEntity.key, chainEntity.name))

                                        Chain.Key(chainEntity.key).matches(chainKey.value)
                                    }
                                    .mapCatching { ChainLinkDataRepository.read(chainKeyEntity).getOrThrow() }
                                    .fold(
                                        onSuccess = { chainLinkEntities ->
                                            SocketMessage.success(SocketMessageType.CHAIN_LINK_READ, Json.encodeToString(chainLinkEntities))
                                        },
                                        onFailure = { exception ->
                                            SocketMessage.failure(SocketMessageType.CHAIN_LINK_READ, exception.message!!)
                                        }
                                    )
                            }
                            SocketMessageType.CHAIN_LINK_UPDATE -> {
                                val chainLinkEntity = Json.decodeFromString<ChainLinkEntity>(fromMessage.data.getOrThrow())

                                ChainDataRepository.read(chainLinkEntity.chainKey.id)
                                    .mapCatching { chainEntity ->
                                        val chainKey = Chain.Key(PasswordEncoder.hash(chainLinkEntity.chainKey.key, chainEntity.name))

                                        Chain.Key(chainEntity.key).matches(chainKey.value)
                                    }
                                    .mapCatching { ChainLinkDataRepository.update(chainLinkEntity).getOrThrow() }
                                    .fold(
                                        onSuccess = {
                                            SocketMessage.success(SocketMessageType.CHAIN_LINK_UPDATE)
                                        },
                                        onFailure = { exception ->
                                            SocketMessage.failure(SocketMessageType.CHAIN_LINK_UPDATE, exception.message!!)
                                        }
                                    )
                            }
                            SocketMessageType.CHAIN_LINK_DELETE -> {
                                val chainLinkEntity = Json.decodeFromString<ChainLinkEntity>(fromMessage.data.getOrThrow())

                                ChainDataRepository.read(chainLinkEntity.chainKey.id)
                                    .mapCatching { chainEntity ->
                                        val chainKey = Chain.Key(PasswordEncoder.hash(chainLinkEntity.chainKey.key, chainEntity.name))

                                        Chain.Key(chainEntity.key).matches(chainKey.value)
                                    }
                                    .mapCatching { ChainLinkDataRepository.delete(chainLinkEntity) }
                                    .fold(
                                        onSuccess = {
                                            SocketMessage.success(SocketMessageType.CHAIN_LINK_DELETE)
                                        },
                                        onFailure = { exception ->
                                            SocketMessage.failure(SocketMessageType.CHAIN_LINK_DELETE, exception.message!!)
                                        }
                                    )
                            }
                        }

                        send(toMessage.toFrame(fromMessage.socketId))
                    }.onFailure { exception -> logger.info(exception.message!!) }
                }
            }

            httpClient.close()
        }
    }
}
