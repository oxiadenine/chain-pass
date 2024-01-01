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
import io.rsocket.kotlin.payload.Payload
import io.sunland.chainpass.common.Chain
import io.sunland.chainpass.common.network.ChainKeyEntity
import io.sunland.chainpass.common.network.ChainLinkEntity
import io.sunland.chainpass.common.network.DiscoverySocket
import io.sunland.chainpass.common.network.WebSocket
import io.sunland.chainpass.common.network.WebSocket.decode
import io.sunland.chainpass.common.network.WebSocket.encode
import io.sunland.chainpass.common.network.WebSocket.getRoute
import io.sunland.chainpass.common.security.EncoderSpec
import io.sunland.chainpass.common.security.PasswordEncoder
import io.sunland.chainpass.server.repository.ChainLinkRepository
import io.sunland.chainpass.server.repository.ChainRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun Application.main() {
    Database.connect(environment.config.config("database"))

    install(WebSockets)
    install(RSocketSupport)
    install(Routing) {
        rSocket {
            RSocketRequestHandler {
                requestResponse { payload ->
                    when (payload.getRoute()) {
                        WebSocket.Route.CHAIN_CREATE -> ChainRepository.create(payload.decode())
                            .mapCatching { Payload.Empty }
                        WebSocket.Route.CHAIN_READ -> ChainRepository.read()
                            .mapCatching { chainEntities -> Payload.encode(chainEntities) }
                        WebSocket.Route.CHAIN_DELETE -> payload.decode<ChainKeyEntity>().let { chainKeyEntity ->
                            ChainRepository.read(chainKeyEntity.id)
                                .mapCatching { chainEntity ->
                                    val key = ChainRepository.key(chainEntity.id).map { key ->
                                        PasswordEncoder.hash(EncoderSpec.Passphrase(chainEntity.key, key.key))
                                    }.getOrThrow()

                                    Chain.Key(key).matches(chainKeyEntity.key).getOrThrow()
                                }
                                .mapCatching { ChainRepository.delete(chainKeyEntity).getOrThrow() }
                                .mapCatching { Payload.Empty }
                        }
                        WebSocket.Route.CHAIN_KEY -> ChainRepository.key(payload.decode())
                            .mapCatching { chainKeyEntity -> Payload.encode(chainKeyEntity) }
                        WebSocket.Route.CHAIN_LINK_CREATE -> payload.decode<ChainLinkEntity>().let { chainLinkEntity ->
                            ChainRepository.read(chainLinkEntity.chainKey.id)
                                .mapCatching { chainEntity ->
                                    val key = ChainRepository.key(chainEntity.id).map { key ->
                                        PasswordEncoder.hash(EncoderSpec.Passphrase(chainEntity.key, key.key))
                                    }.getOrThrow()

                                    Chain.Key(key).matches(chainLinkEntity.chainKey.key).getOrThrow()
                                }
                                .mapCatching { ChainLinkRepository.create(chainLinkEntity).getOrThrow() }
                                .mapCatching { Payload.Empty }
                        }
                        WebSocket.Route.CHAIN_LINK_READ -> payload.decode<ChainKeyEntity>().let { chainKeyEntity ->
                            ChainRepository.read(chainKeyEntity.id)
                                .mapCatching { chainEntity ->
                                    val key = ChainRepository.key(chainEntity.id).map { key ->
                                        PasswordEncoder.hash(EncoderSpec.Passphrase(chainEntity.key, key.key))
                                    }.getOrThrow()

                                    Chain.Key(key).matches(chainKeyEntity.key).getOrThrow()
                                }
                                .mapCatching { ChainLinkRepository.read(chainKeyEntity).getOrThrow() }
                                .mapCatching { chainLinkEntities -> Payload.encode(chainLinkEntities) }
                        }
                        WebSocket.Route.CHAIN_LINK_UPDATE -> payload.decode<ChainLinkEntity>().let { chainLinkEntity ->
                            ChainRepository.read(chainLinkEntity.chainKey.id)
                                .mapCatching { chainEntity ->
                                    val key = ChainRepository.key(chainEntity.id).map { key ->
                                        PasswordEncoder.hash(EncoderSpec.Passphrase(chainEntity.key, key.key))
                                    }.getOrThrow()

                                    Chain.Key(key).matches(chainLinkEntity.chainKey.key).getOrThrow()
                                }
                                .mapCatching { ChainLinkRepository.update(chainLinkEntity).getOrThrow() }
                                .mapCatching { Payload.Empty }
                        }
                        WebSocket.Route.CHAIN_LINK_DELETE -> payload.decode<ChainLinkEntity>().let { chainLinkEntity ->
                            ChainRepository.read(chainLinkEntity.chainKey.id)
                                .mapCatching { chainEntity ->
                                    val key = ChainRepository.key(chainEntity.id).map { key ->
                                        PasswordEncoder.hash(EncoderSpec.Passphrase(chainEntity.key, key.key))
                                    }.getOrThrow()

                                    Chain.Key(key).matches(chainLinkEntity.chainKey.key).getOrThrow()
                                }
                                .mapCatching { ChainLinkRepository.delete(chainLinkEntity).getOrThrow() }
                                .mapCatching { Payload.Empty }
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
        InetSocketAddress(DiscoverySocket.HOST, DiscoverySocket.PORT)
    )

    environment.log.info("Discovery listening at ${socket.localAddress.toJavaAddress()}")

    launch(Dispatchers.IO) {
        while (true) {
            val datagram = socket.receive()

            if (datagram.packet.readText() == DiscoverySocket.MESSAGE) {
                socket.send(Datagram(ByteReadPacket(discoveryAddress.toByteArray()), datagram.address))
            }

            datagram.packet.close()
        }
    }
}