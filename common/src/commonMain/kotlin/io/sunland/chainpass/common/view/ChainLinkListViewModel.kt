package io.sunland.chainpass.common.view

import androidx.compose.runtime.mutableStateListOf
import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.http.cio.websocket.*
import io.sunland.chainpass.common.network.SocketMessage
import io.sunland.chainpass.common.network.SocketMessageType
import io.sunland.chainpass.common.repository.Chain
import io.sunland.chainpass.common.repository.ChainLink
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ChainLinkListViewModel(private val httpClient: HttpClient) : ViewModel {
    var chainListItem: ChainListItem? = null

    val chainLinkListItems = mutableStateListOf<ChainLinkListItem>()

    override fun refresh() {
        val chainLinkItems = chainLinkListItems.toList()

        chainLinkListItems.clear()
        chainLinkListItems.addAll(chainLinkItems)
    }

    suspend fun create(chainLinkListItem: ChainLinkListItem) = runCatching {
        httpClient.webSocket {
            val chainLink = ChainLink(
                chainLinkListItem.id,
                chainLinkListItem.name,
                chainLinkListItem.password,
                chainLinkListItem.chainId
            )

            send(SocketMessage(SocketMessageType.CHAIN_LINK_CREATE, Json.encodeToString(chainLink)).toFrame())

            while (true) {
                val frame = incoming.receive() as? Frame.Text ?: continue

                val message = SocketMessage.from(frame)

                if (message.type == SocketMessageType.CHAIN_LINK_CREATE) {
                    chainLinkListItem.id = Json.decodeFromString<ChainLink>(message.text).id
                }

                return@webSocket
            }
        }
    }

    suspend fun read() = runCatching {
        httpClient.webSocket {
            val chain = Chain(chainListItem!!.id, chainListItem!!.name, chainListItem!!.key)

            send(SocketMessage(SocketMessageType.CHAIN_LINK_READ, Json.encodeToString(chain)).toFrame())

            while (true) {
                val frame = incoming.receive() as? Frame.Text ?: continue

                val message = SocketMessage.from(frame)

                if (message.type == SocketMessageType.CHAIN_LINK_READ) {
                    chainLinkListItems.clear()
                    chainLinkListItems.addAll(Json.decodeFromString<List<ChainLink>>(message.text).map { chainLink ->
                        ChainLinkListItem(
                            chainLink.id,
                            chainLink.name,
                            chainLink.password,
                            chainLink.chainId,
                            ChainLinkListItemStatus.ACTUAL
                        )
                    })
                }

                return@webSocket
            }
        }
    }

    suspend fun update(chainLinkListItem: ChainLinkListItem) = runCatching {
        httpClient.webSocket {
            val chainLink = ChainLink(
                chainLinkListItem.id,
                chainLinkListItem.name,
                chainLinkListItem.password,
                chainLinkListItem.chainId
            )

            send(SocketMessage(SocketMessageType.CHAIN_LINK_UPDATE, Json.encodeToString(chainLink)).toFrame())
        }
    }

    suspend fun delete(chainLinkListItem: ChainLinkListItem) = runCatching {
        httpClient.webSocket {
            val chainLink = ChainLink(
                chainLinkListItem.id,
                chainLinkListItem.name,
                chainLinkListItem.password,
                chainLinkListItem.chainId
            )

            send(SocketMessage(SocketMessageType.CHAIN_LINK_DELETE, Json.encodeToString(chainLink)).toFrame())
        }
    }
}
