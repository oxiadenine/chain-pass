package io.sunland.chainpass.common.repository

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import io.sunland.chainpass.common.network.SocketMessage
import io.sunland.chainpass.common.network.SocketRoute
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ChainLinkNetRepository(private val httpClient: HttpClient) : ChainLinkRepository {
    override suspend fun create(chainLinkEntity: ChainLinkEntity) = runCatching {
        httpClient.webSocket(path = SocketRoute.CHAIN_LINK_CREATE.path) {
            send(SocketMessage.success(Json.encodeToString(chainLinkEntity)).toFrame())

            val message = SocketMessage.from(incoming.receive() as Frame.Text)

            chainLinkEntity.id = Json.decodeFromString<ChainLinkEntity>(message.data.getOrThrow()).id
        }

        chainLinkEntity.id
    }

    override suspend fun read(chainKeyEntity: ChainKeyEntity) = runCatching {
        val chainLinkEntities = mutableListOf<ChainLinkEntity>()

        httpClient.webSocket(path = SocketRoute.CHAIN_LINK_READ.path) {
            send(SocketMessage.success(Json.encodeToString(chainKeyEntity)).toFrame())

            val message = SocketMessage.from(incoming.receive() as Frame.Text)

            chainLinkEntities.addAll(Json.decodeFromString<List<ChainLinkEntity>>(message.data.getOrThrow()))
        }

        chainLinkEntities.toList()
    }

    override suspend fun update(chainLinkEntity: ChainLinkEntity) = runCatching {
        httpClient.webSocket(path = SocketRoute.CHAIN_LINK_UPDATE.path) {
            send(SocketMessage.success(Json.encodeToString(chainLinkEntity)).toFrame())

            val message = SocketMessage.from(incoming.receive() as Frame.Text)

            message.data.getOrThrow()
        }
    }

    override suspend fun delete(chainLinkEntity: ChainLinkEntity) = runCatching {
        httpClient.webSocket(path = SocketRoute.CHAIN_LINK_DELETE.path) {
            send(SocketMessage.success(Json.encodeToString(chainLinkEntity)).toFrame())

            val message = SocketMessage.from(incoming.receive() as Frame.Text)

            message.data.getOrThrow()
        }
    }
}