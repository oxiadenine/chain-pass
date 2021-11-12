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
    override suspend fun create(chain: Chain) = runCatching {
        httpClient.webSocket {
            send(SocketMessage(SocketMessageType.CHAIN_CREATE, Json.encodeToString(chain)).toFrame())

            while (true) {
                val frame = incoming.receive() as? Frame.Text ?: continue

                val message = SocketMessage.from(frame)

                if (message.type == SocketMessageType.CHAIN_CREATE) {
                    chain.id = Json.decodeFromString<Chain>(message.text).id
                }

                break
            }
        }

        chain.id
    }

    override suspend fun read() = runCatching {
        val chains = mutableListOf<Chain>()

        httpClient.webSocket {
            send(SocketMessage(SocketMessageType.CHAIN_READ).toFrame())

            while (true) {
                val frame = incoming.receive() as? Frame.Text ?: continue

                val message = SocketMessage.from(frame)

                if (message.type == SocketMessageType.CHAIN_READ) {
                    chains.addAll(Json.decodeFromString<List<Chain>>(message.text))
                }

                break
            }
        }

        chains.toList()
    }

    override suspend fun delete(chain: Chain) = runCatching {
        httpClient.webSocket {
            send(SocketMessage(SocketMessageType.CHAIN_DELETE, Json.encodeToString(chain)).toFrame())
        }
    }
}
