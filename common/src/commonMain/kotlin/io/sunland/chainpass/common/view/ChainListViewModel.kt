package io.sunland.chainpass.common.view

import androidx.compose.runtime.mutableStateListOf
import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.http.cio.websocket.*
import io.sunland.chainpass.common.SocketMessage
import io.sunland.chainpass.common.SocketMessageType
import io.sunland.chainpass.common.repository.Chain
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ChainListViewModel(private val httpClient: HttpClient) : ViewModel {
    val chainListItems = mutableStateListOf<ChainListItem>()

    override fun refresh() {
        val chainItems = chainListItems.toList()

        chainListItems.clear()
        chainListItems.addAll(chainItems)
    }

    suspend fun create(chainListItem: ChainListItem) = runCatching {
        httpClient.webSocket {
            val socketId = call.request.headers["Socket-Id"]!!

            val chain = Chain(chainListItem.id, chainListItem.name, chainListItem.key)

            send(SocketMessage(SocketMessageType.CHAIN_CREATE, socketId, Json.encodeToString(chain)).toFrame())

            while (true) {
                val frame = incoming.receive() as? Frame.Text ?: continue

                val message = SocketMessage.from(frame)

                if (message.type == SocketMessageType.CHAIN_CREATE) {
                    chainListItem.id = Json.decodeFromString<Chain>(message.text).id
                }

                break
            }

            close()
        }
    }

    suspend fun read() = runCatching {
        httpClient.webSocket {
            val socketId = call.request.headers["Socket-Id"]!!

            send(SocketMessage(SocketMessageType.CHAIN_READ, socketId).toFrame())

            while (true) {
                val frame = incoming.receive() as? Frame.Text ?: continue

                val message = SocketMessage.from(frame)

                if (message.type == SocketMessageType.CHAIN_READ) {
                    chainListItems.clear()
                    chainListItems.addAll(Json.decodeFromString<List<Chain>>(message.text).map {
                        ChainListItem(it.id, it.name, it.key, ChainListItemStatus.ACTUAL)
                    })
                }

                break
            }

            close()
        }
    }

    suspend fun delete(chainListItem: ChainListItem) = runCatching {
        httpClient.webSocket {
            val socketId = call.request.headers["Socket-Id"]!!

            val chain = Chain(chainListItem.id, chainListItem.name, chainListItem.key)

            send(SocketMessage(SocketMessageType.CHAIN_DELETE, socketId, Json.encodeToString(chain)).toFrame())
            close()
        }
    }
}
