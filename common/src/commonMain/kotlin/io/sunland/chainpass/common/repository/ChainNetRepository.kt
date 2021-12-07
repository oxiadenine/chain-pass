package io.sunland.chainpass.common.repository

import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.http.cio.websocket.*
import io.sunland.chainpass.common.network.SocketMessage
import io.sunland.chainpass.common.network.SocketRoute
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ChainNetRepository(private val httpClient: HttpClient) : ChainRepository {
    override suspend fun create(chainEntity: ChainEntity) = runCatching {
        httpClient.webSocket(path = SocketRoute.CHAIN_CREATE.path) {
            send(SocketMessage.success(Json.encodeToString(chainEntity)).toFrame())

            val message = SocketMessage.from(incoming.receive() as Frame.Text)

            chainEntity.id = Json.decodeFromString<ChainEntity>(message.data.getOrThrow()).id
        }

        chainEntity.id
    }

    override suspend fun read() = runCatching {
        val chainEntities = mutableListOf<ChainEntity>()

        httpClient.webSocket(path = SocketRoute.CHAIN_READ.path) {
            send(SocketMessage.success().toFrame())

            val message = SocketMessage.from(incoming.receive() as Frame.Text)

            chainEntities.addAll(Json.decodeFromString<List<ChainEntity>>(message.data.getOrThrow()))
        }

        chainEntities.toList()
    }

    override suspend fun read(id: Int): Result<ChainEntity> {
        throw NotImplementedError("Not yet implemented")
    }

    override suspend fun delete(chainKeyEntity: ChainKeyEntity) = runCatching {
        httpClient.webSocket(path = SocketRoute.CHAIN_DELETE.path) {
            send(SocketMessage.success(Json.encodeToString(chainKeyEntity)).toFrame())

            val message = SocketMessage.from(incoming.receive() as Frame.Text)

            message.data.getOrThrow()
        }
    }

    override suspend fun key(id: Int) = runCatching {
        var key: ChainKeyEntity? = null

        httpClient.webSocket(path = SocketRoute.CHAIN_KEY.path) {
            send(SocketMessage.success(id.toString()).toFrame())

            val message = SocketMessage.from(incoming.receive() as Frame.Text)

            key = Json.decodeFromString(message.data.getOrThrow())
        }

        key!!
    }
}
