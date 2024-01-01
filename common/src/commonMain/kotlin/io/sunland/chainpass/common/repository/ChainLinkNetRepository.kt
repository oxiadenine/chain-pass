package io.sunland.chainpass.common.repository

import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.http.cio.websocket.*
import io.sunland.chainpass.common.network.SocketMessage
import io.sunland.chainpass.common.network.SocketMessageType
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ChainLinkNetRepository(private val httpClient: HttpClient) : ChainLinkRepository {
    override suspend fun create(chainLinkEntity: ChainLinkEntity) = runCatching {
        httpClient.webSocket {
            send(SocketMessage(SocketMessageType.CHAIN_LINK_CREATE, Json.encodeToString(chainLinkEntity)).toFrame())

            while (true) {
                val frame = incoming.receive() as? Frame.Text ?: continue

                val message = SocketMessage.from(frame)

                if (message.type == SocketMessageType.CHAIN_LINK_CREATE) {
                    chainLinkEntity.id = Json.decodeFromString<ChainLinkEntity>(message.text).id
                }

                break
            }
        }

        chainLinkEntity.id
    }

    override suspend fun read(chainKeyEntity: ChainKeyEntity) = runCatching {
        val chainLinkEntities = mutableListOf<ChainLinkEntity>()

        httpClient.webSocket {
            send(SocketMessage(SocketMessageType.CHAIN_LINK_READ, Json.encodeToString(chainKeyEntity)).toFrame())

            while (true) {
                val frame = incoming.receive() as? Frame.Text ?: continue

                val message = SocketMessage.from(frame)

                if (message.type == SocketMessageType.CHAIN_LINK_READ) {
                    chainLinkEntities.addAll(Json.decodeFromString<List<ChainLinkEntity>>(message.text))
                }

                break
            }
        }

        chainLinkEntities.toList()
    }

    override suspend fun update(chainLinkEntity: ChainLinkEntity) = runCatching {
        httpClient.webSocket {
            send(SocketMessage(SocketMessageType.CHAIN_LINK_UPDATE, Json.encodeToString(chainLinkEntity)).toFrame())
        }
    }

    override suspend fun delete(chainLinkEntity: ChainLinkEntity) = runCatching {
        httpClient.webSocket {
            send(SocketMessage(SocketMessageType.CHAIN_LINK_DELETE, Json.encodeToString(chainLinkEntity)).toFrame())
        }
    }
}
