package io.sunland.chainpass.service

import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import io.sunland.chainpass.common.Chain
import io.sunland.chainpass.common.network.SocketMessage
import io.sunland.chainpass.common.network.SocketRoute
import io.sunland.chainpass.common.repository.ChainEntity
import io.sunland.chainpass.common.repository.ChainKeyEntity
import io.sunland.chainpass.common.repository.ChainLinkEntity
import io.sunland.chainpass.common.security.EncoderSpec
import io.sunland.chainpass.common.security.PasswordEncoder
import io.sunland.chainpass.service.repository.ChainDataRepository
import io.sunland.chainpass.service.repository.ChainLinkDataRepository
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun Application.main() {
    install(WebSockets)

    Database.connect(environment.config.config("database"))

    routing {
        webSocket(SocketRoute.CHAIN_CREATE.path) {
            runCatching {
                val message = SocketMessage.from(incoming.receive() as Frame.Text)

                val chainEntity = Json.decodeFromString<ChainEntity>(message.data.getOrThrow())

                ChainDataRepository.create(chainEntity).fold(
                    onSuccess = { chainEntityId ->
                        chainEntity.id = chainEntityId

                        SocketMessage.success(Json.encodeToString(chainEntity))
                    },
                    onFailure = { exception -> SocketMessage.failure(exception.message) }
                )
            }.fold(
                onSuccess = { message -> send(message.toFrame()) },
                onFailure = { exception -> log.info(exception.message) }
            )

            close()
        }

        webSocket(SocketRoute.CHAIN_READ.path) {
            runCatching {
                SocketMessage.from(incoming.receive() as Frame.Text).data.getOrThrow()

                ChainDataRepository.read().fold(
                    onSuccess = { chainEntities -> SocketMessage.success(Json.encodeToString(chainEntities)) },
                    onFailure = { exception -> SocketMessage.failure(exception.message) }
                )
            }.fold(
                onSuccess = { message -> send(message.toFrame()) },
                onFailure = { exception -> log.info(exception.message) }
            )

            close()
        }

        webSocket(SocketRoute.CHAIN_DELETE.path) {
            runCatching {
                val message = SocketMessage.from(incoming.receive() as Frame.Text)

                val chainKeyEntity = Json.decodeFromString<ChainKeyEntity>(message.data.getOrThrow())

                ChainDataRepository.read(chainKeyEntity.id)
                    .mapCatching { chainEntity ->
                        val key = ChainDataRepository.key(chainEntity.id).map { key ->
                            PasswordEncoder.hash(EncoderSpec.Passphrase(chainEntity.key, key.key))
                        }.getOrThrow()

                        Chain.Key(key).matches(chainKeyEntity.key)
                    }
                    .mapCatching { ChainDataRepository.delete(chainKeyEntity).getOrThrow() }
                    .fold(
                        onSuccess = { SocketMessage.success() },
                        onFailure = { exception -> SocketMessage.failure(exception.message) }
                    )
            }.fold(
                onSuccess = { message -> send(message.toFrame()) },
                onFailure = { exception -> log.info(exception.message) }
            )

            close()
        }

        webSocket(SocketRoute.CHAIN_KEY.path) {
            runCatching {
                val message = SocketMessage.from(incoming.receive() as Frame.Text)

                val chainEntityId = message.data.getOrThrow().toInt()

                ChainDataRepository.key(chainEntityId).fold(
                    onSuccess = { key -> SocketMessage.success(Json.encodeToString(key)) },
                    onFailure = { exception -> SocketMessage.failure(exception.message) }
                )
            }.fold(
                onSuccess = { message -> send(message.toFrame()) },
                onFailure = { exception -> log.info(exception.message) }
            )

            close()
        }

        webSocket(SocketRoute.CHAIN_LINK_CREATE.path) {
            runCatching {
                val message = SocketMessage.from(incoming.receive() as Frame.Text)

                val chainLinkEntity = Json.decodeFromString<ChainLinkEntity>(message.data.getOrThrow())

                ChainDataRepository.read(chainLinkEntity.chainKey.id)
                    .mapCatching { chainEntity ->
                        val key = ChainDataRepository.key(chainEntity.id).map { key ->
                            PasswordEncoder.hash(EncoderSpec.Passphrase(chainEntity.key, key.key))
                        }.getOrThrow()

                        Chain.Key(key).matches(chainLinkEntity.chainKey.key)
                    }
                    .mapCatching { ChainLinkDataRepository.create(chainLinkEntity).getOrThrow() }
                    .fold(
                        onSuccess = { chainLinkEntityId ->
                            chainLinkEntity.id = chainLinkEntityId

                            SocketMessage.success(Json.encodeToString(chainLinkEntity))
                        },
                        onFailure = { exception -> SocketMessage.failure(exception.message) }
                    )
            }.fold(
                onSuccess = { message -> send(message.toFrame()) },
                onFailure = { exception -> log.info(exception.message) }
            )

            close()
        }

        webSocket(SocketRoute.CHAIN_LINK_READ.path) {
            runCatching {
                val message = SocketMessage.from(incoming.receive() as Frame.Text)

                val chainKeyEntity = Json.decodeFromString<ChainKeyEntity>(message.data.getOrThrow())

                ChainDataRepository.read(chainKeyEntity.id)
                    .mapCatching { chainEntity ->
                        val key = ChainDataRepository.key(chainEntity.id).map { key ->
                            PasswordEncoder.hash(EncoderSpec.Passphrase(chainEntity.key, key.key))
                        }.getOrThrow()

                        Chain.Key(key).matches(chainKeyEntity.key)
                    }
                    .mapCatching { ChainLinkDataRepository.read(chainKeyEntity).getOrThrow() }
                    .fold(
                        onSuccess = { chainLinkEntities -> SocketMessage.success(Json.encodeToString(chainLinkEntities)) },
                        onFailure = { exception -> SocketMessage.failure(exception.message) }
                    )
            }.fold(
                onSuccess = { message -> send(message.toFrame()) },
                onFailure = { exception -> log.info(exception.message) }
            )

            close()
        }

        webSocket(SocketRoute.CHAIN_LINK_UPDATE.path) {
            runCatching {
                val message = SocketMessage.from(incoming.receive() as Frame.Text)

                val chainLinkEntity = Json.decodeFromString<ChainLinkEntity>(message.data.getOrThrow())

                ChainDataRepository.read(chainLinkEntity.chainKey.id)
                    .mapCatching { chainEntity ->
                        val key = ChainDataRepository.key(chainEntity.id).map { key ->
                            PasswordEncoder.hash(EncoderSpec.Passphrase(chainEntity.key, key.key))
                        }.getOrThrow()

                        Chain.Key(key).matches(chainLinkEntity.chainKey.key)
                    }
                    .mapCatching { ChainLinkDataRepository.update(chainLinkEntity).getOrThrow() }
                    .fold(
                        onSuccess = { SocketMessage.success() },
                        onFailure = { exception -> SocketMessage.failure(exception.message) }
                    )
            }.fold(
                onSuccess = { message -> send(message.toFrame()) },
                onFailure = { exception -> log.info(exception.message) }
            )

            close()
        }

        webSocket(SocketRoute.CHAIN_LINK_DELETE.path) {
            runCatching {
                val message = SocketMessage.from(incoming.receive() as Frame.Text)

                val chainLinkEntity = Json.decodeFromString<ChainLinkEntity>(message.data.getOrThrow())

                ChainDataRepository.read(chainLinkEntity.chainKey.id)
                    .mapCatching { chainEntity ->
                        val key = ChainDataRepository.key(chainEntity.id).map { key ->
                            PasswordEncoder.hash(EncoderSpec.Passphrase(chainEntity.key, key.key))
                        }.getOrThrow()

                        Chain.Key(key).matches(chainLinkEntity.chainKey.key)
                    }
                    .mapCatching { ChainLinkDataRepository.delete(chainLinkEntity) }
                    .fold(
                        onSuccess = { SocketMessage.success() },
                        onFailure = { exception -> SocketMessage.failure(exception.message) }
                    )
            }.fold(
                onSuccess = { message -> send(message.toFrame()) },
                onFailure = { exception -> log.info(exception.message) }
            )

            close()
        }
    }
}
