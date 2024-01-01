package io.sunland.chainpass.server

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.utils.io.core.*
import io.rsocket.kotlin.RSocketRequestHandler
import io.rsocket.kotlin.ktor.server.RSocketSupport
import io.rsocket.kotlin.ktor.server.rSocket
import io.sunland.chainpass.common.Chain
import io.sunland.chainpass.common.network.*
import io.sunland.chainpass.common.repository.ChainEntity
import io.sunland.chainpass.common.repository.ChainKeyEntity
import io.sunland.chainpass.common.repository.ChainLinkEntity
import io.sunland.chainpass.common.security.EncoderSpec
import io.sunland.chainpass.common.security.PasswordEncoder
import io.sunland.chainpass.server.repository.ChainDataRepository
import io.sunland.chainpass.server.repository.ChainLinkDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

fun Application.main() {
    Database.connect(environment.config.config("database"))

    install(WebSockets)
    install(RSocketSupport)
    install(Routing) {
        rSocket {
            RSocketRequestHandler {
                requestResponse { payload ->
                    when (payload.getRoute()) {
                        PayloadRoute.CHAIN_CREATE -> {
                            val chainEntity = Json.decodeFromPayload<ChainEntity>(payload)

                            ChainDataRepository.create(chainEntity).mapCatching { Payload() }
                        }
                        PayloadRoute.CHAIN_READ -> {
                            ChainDataRepository.read().mapCatching { chainEntities ->
                                Json.encodeToPayload(chainEntities)
                            }
                        }
                        PayloadRoute.CHAIN_DELETE -> {
                            val chainKeyEntity = Json.decodeFromPayload<ChainKeyEntity>(payload)

                            ChainDataRepository.read(chainKeyEntity.id)
                                .mapCatching { chainEntity ->
                                    val key = ChainDataRepository.key(chainEntity.id).map { key ->
                                        PasswordEncoder.hash(EncoderSpec.Passphrase(chainEntity.key, key.key))
                                    }.getOrThrow()

                                    Chain.Key(key).matches(chainKeyEntity.key).getOrThrow()
                                }
                                .mapCatching { ChainDataRepository.delete(chainKeyEntity).getOrThrow() }
                                .mapCatching { Payload() }
                        }
                        PayloadRoute.CHAIN_KEY -> {
                            val chainEntityId = Json.decodeFromPayload<Int>(payload)

                            ChainDataRepository.key(chainEntityId).mapCatching { chainKeyEntity ->
                                Json.encodeToPayload(chainKeyEntity)
                            }
                        }
                        PayloadRoute.CHAIN_LINK_CREATE -> {
                            val chainLinkEntity = Json.decodeFromPayload<ChainLinkEntity>(payload)

                            ChainDataRepository.read(chainLinkEntity.chainKey.id)
                                .mapCatching { chainEntity ->
                                    val key = ChainDataRepository.key(chainEntity.id).map { key ->
                                        PasswordEncoder.hash(EncoderSpec.Passphrase(chainEntity.key, key.key))
                                    }.getOrThrow()

                                    Chain.Key(key).matches(chainLinkEntity.chainKey.key).getOrThrow()
                                }
                                .mapCatching { ChainLinkDataRepository.create(chainLinkEntity).getOrThrow() }
                                .mapCatching { Payload() }
                        }
                        PayloadRoute.CHAIN_LINK_READ -> {
                            val chainKeyEntity = Json.decodeFromPayload<ChainKeyEntity>(payload)

                            ChainDataRepository.read(chainKeyEntity.id)
                                .mapCatching { chainEntity ->
                                    val key = ChainDataRepository.key(chainEntity.id).map { key ->
                                        PasswordEncoder.hash(EncoderSpec.Passphrase(chainEntity.key, key.key))
                                    }.getOrThrow()

                                    Chain.Key(key).matches(chainKeyEntity.key).getOrThrow()
                                }
                                .mapCatching { ChainLinkDataRepository.read(chainKeyEntity).getOrThrow() }
                                .mapCatching { chainLinkEntities ->
                                    Json.encodeToPayload(chainLinkEntities)
                                }
                        }
                        PayloadRoute.CHAIN_LINK_UPDATE -> {
                            val chainLinkEntity = Json.decodeFromPayload<ChainLinkEntity>(payload)

                            ChainDataRepository.read(chainLinkEntity.chainKey.id)
                                .mapCatching { chainEntity ->
                                    val key = ChainDataRepository.key(chainEntity.id).map { key ->
                                        PasswordEncoder.hash(EncoderSpec.Passphrase(chainEntity.key, key.key))
                                    }.getOrThrow()

                                    Chain.Key(key).matches(chainLinkEntity.chainKey.key).getOrThrow()
                                }
                                .mapCatching { ChainLinkDataRepository.update(chainLinkEntity).getOrThrow() }
                                .mapCatching { Payload() }
                        }
                        PayloadRoute.CHAIN_LINK_DELETE -> {
                            val chainLinkEntity = Json.decodeFromPayload<ChainLinkEntity>(payload)

                            ChainDataRepository.read(chainLinkEntity.chainKey.id)
                                .mapCatching { chainEntity ->
                                    val key = ChainDataRepository.key(chainEntity.id).map { key ->
                                        PasswordEncoder.hash(EncoderSpec.Passphrase(chainEntity.key, key.key))
                                    }.getOrThrow()

                                    Chain.Key(key).matches(chainLinkEntity.chainKey.key).getOrThrow()
                                }
                                .mapCatching { ChainLinkDataRepository.delete(chainLinkEntity).getOrThrow() }
                                .mapCatching { Payload() }
                        }
                        else -> error("No payload route found")
                    }.getOrThrow()
                }
            }
        }
    }
}

fun Application.discovery() {
    val discoveryAddress = environment.config.property("server.discoveryAddress").getString()

    val socket = aSocket(ActorSelectorManager(Dispatchers.IO)).udp().bind(
        InetSocketAddress(SocketConfig.HOST, SocketConfig.PORT)
    )

    environment.log.info("Discovery listening at ${socket.localAddress.toJavaAddress()}")

    launch(Dispatchers.IO) {
        while (true) {
            val datagram = socket.receive()

            if (datagram.packet.readText() == SocketConfig.MESSAGE) {
                socket.send(Datagram(ByteReadPacket(discoveryAddress.toByteArray()), datagram.address))
            }

            datagram.packet.close()
        }
    }
}