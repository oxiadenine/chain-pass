package io.sunland.chainpass.common.repository

import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.http.cio.websocket.*
import io.sunland.chainpass.common.network.SocketMessage
import io.sunland.chainpass.common.network.SocketMessageType
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ChainNetRepository(private val httpClient: HttpClient) : ChainRepository {
    override suspend fun create(chainEntity: ChainEntity) = runCatching {
        httpClient.webSocket {
            send(SocketMessage.success(SocketMessageType.CHAIN_CREATE, Json.encodeToString(chainEntity)).toFrame())

            while (true) {
                val frame = incoming.receive() as? Frame.Text ?: continue

                val message = SocketMessage.from(frame)

                if (message.type == SocketMessageType.CHAIN_CREATE) {
                    chainEntity.id = Json.decodeFromString<ChainEntity>(message.data.getOrThrow()).id
                }

                break
            }
        }

        chainEntity.id
    }

    override suspend fun read() = runCatching {
        val chainEntities = mutableListOf<ChainEntity>()

        httpClient.webSocket {
            send(SocketMessage.success(SocketMessageType.CHAIN_READ).toFrame())

            while (true) {
                val frame = incoming.receive() as? Frame.Text ?: continue

                val message = SocketMessage.from(frame)

                if (message.type == SocketMessageType.CHAIN_READ) {
                    chainEntities.addAll(Json.decodeFromString<List<ChainEntity>>(message.data.getOrThrow()))
                }

                break
            }
        }

        chainEntities.toList()
    }

    override suspend fun read(id: Int): Result<ChainEntity> {
        throw NotImplementedError("Not yet implemented")
    }

    override suspend fun delete(chainKeyEntity: ChainKeyEntity) = runCatching {
        httpClient.webSocket {
            send(SocketMessage.success(SocketMessageType.CHAIN_DELETE, Json.encodeToString(chainKeyEntity)).toFrame())

            while (true) {
                val frame = incoming.receive() as? Frame.Text ?: continue

                val message = SocketMessage.from(frame)

                if (message.type == SocketMessageType.CHAIN_DELETE) {
                    message.data.getOrThrow()
                }

                break
            }
        }
    }

    override suspend fun key(id: Int) = runCatching {
        var key: ChainKeyEntity? = null

        httpClient.webSocket {
            send(SocketMessage.success(SocketMessageType.CHAIN_KEY, id.toString()).toFrame())

            while (true) {
                val frame = incoming.receive() as? Frame.Text ?: continue

                val message = SocketMessage.from(frame)

                if (message.type == SocketMessageType.CHAIN_KEY) {
                    key = Json.decodeFromString(message.data.getOrThrow())
                }

                break
            }
        }

        key!!
    }
}
