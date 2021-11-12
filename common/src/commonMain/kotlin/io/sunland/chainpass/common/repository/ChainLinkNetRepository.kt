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
    override suspend fun create(chainLink: ChainLink) = runCatching {
        httpClient.webSocket {
            send(SocketMessage(SocketMessageType.CHAIN_LINK_CREATE, Json.encodeToString(chainLink)).toFrame())

            while (true) {
                val frame = incoming.receive() as? Frame.Text ?: continue

                val message = SocketMessage.from(frame)

                if (message.type == SocketMessageType.CHAIN_LINK_CREATE) {
                    chainLink.id = Json.decodeFromString<ChainLink>(message.text).id
                }

                break
            }
        }

        chainLink.id
    }

    override suspend fun read(chain: Chain) = runCatching {
        val chainLinks = mutableListOf<ChainLink>()

        httpClient.webSocket {
            send(SocketMessage(SocketMessageType.CHAIN_LINK_READ, Json.encodeToString(chain)).toFrame())

            while (true) {
                val frame = incoming.receive() as? Frame.Text ?: continue

                val message = SocketMessage.from(frame)

                if (message.type == SocketMessageType.CHAIN_LINK_READ) {
                    chainLinks.addAll(Json.decodeFromString<List<ChainLink>>(message.text))
                }

                break
            }
        }

        chainLinks.toList()
    }

    override suspend fun update(chainLink: ChainLink) = runCatching {
        httpClient.webSocket {
            send(SocketMessage(SocketMessageType.CHAIN_LINK_UPDATE, Json.encodeToString(chainLink)).toFrame())
        }
    }

    override suspend fun delete(chainLink: ChainLink) = runCatching {
        httpClient.webSocket {
            send(SocketMessage(SocketMessageType.CHAIN_LINK_DELETE, Json.encodeToString(chainLink)).toFrame())
        }
    }
}
