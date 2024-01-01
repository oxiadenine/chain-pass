package io.sunland.chainpass.common.view

import androidx.compose.runtime.mutableStateListOf
import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.http.cio.websocket.*
import io.sunland.chainpass.common.network.SocketMessage
import io.sunland.chainpass.common.network.SocketMessageType
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
            val chain = Chain(chainListItem.id, chainListItem.name, chainListItem.key)

            send(SocketMessage(SocketMessageType.CHAIN_CREATE, Json.encodeToString(chain)).toFrame())

            while (true) {
                val frame = incoming.receive() as? Frame.Text ?: continue

                val message = SocketMessage.from(frame)

                if (message.type == SocketMessageType.CHAIN_CREATE) {
                    chainListItem.id = Json.decodeFromString<Chain>(message.text).id
                }

                return@webSocket
            }
        }
    }

    suspend fun read() = runCatching {
        httpClient.webSocket {
            send(SocketMessage(SocketMessageType.CHAIN_READ).toFrame())

            while (true) {
                val frame = incoming.receive() as? Frame.Text ?: continue

                val message = SocketMessage.from(frame)

                if (message.type == SocketMessageType.CHAIN_READ) {
                    chainListItems.clear()
                    chainListItems.addAll(Json.decodeFromString<List<Chain>>(message.text).map { chain ->
                        ChainListItem(chain.id, chain.name, chain.key, ChainListItemStatus.ACTUAL)
                    })
                }

                return@webSocket
            }
        }
    }

    suspend fun delete(chainListItem: ChainListItem) = runCatching {
        httpClient.webSocket {
            val chain = Chain(chainListItem.id, chainListItem.name, chainListItem.key)

            send(SocketMessage(SocketMessageType.CHAIN_DELETE, Json.encodeToString(chain)).toFrame())
        }
    }
}
