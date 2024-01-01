package io.sunland.chainpass.common.view

import androidx.compose.runtime.mutableStateListOf
import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.http.cio.websocket.*
import io.sunland.chainpass.common.*
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

    suspend fun create(chainLinkListItem: ChainLinkListItem) = httpClient.webSocket {
        val chainLink = ChainLink(
            chainLinkListItem.id,
            chainLinkListItem.name,
            chainLinkListItem.password,
            chainLinkListItem.chainId
        )

        send(WebSocket.Message(Json.encodeToString(chainLink), WebSocket.MessageType.CREATE_CHAIN_LINK).toFrame())

        while (true) {
            val frame = incoming.receive() as? Frame.Text ?: continue

            val message = WebSocket.Message.from(frame)

            if (message.type == WebSocket.MessageType.CREATE_CHAIN_LINK) {
                chainLinkListItem.id = Json.decodeFromString<ChainLink>(message.text).id
            }

            break
        }

        close()
    }

    suspend fun read() = httpClient.webSocket {
        val chain = Chain(chainListItem!!.id, chainListItem!!.name, chainListItem!!.key)

        send(WebSocket.Message(Json.encodeToString(chain), WebSocket.MessageType.READ_CHAIN_LINK).toFrame())

        while (true) {
            val frame = incoming.receive() as? Frame.Text ?: continue

            val message = WebSocket.Message.from(frame)

            if (message.type == WebSocket.MessageType.READ_CHAIN_LINK) {
                chainLinkListItems.clear()
                chainLinkListItems.addAll(Json.decodeFromString<List<ChainLink>>(message.text).map {
                    ChainLinkListItem(it.id, it.name, it.password, it.chainId, ChainLinkListItemStatus.ACTUAL)
                })
            }

            break
        }

        close()
    }

    suspend fun update(chainLinkListItem: ChainLinkListItem) = httpClient.webSocket {
        val chainLink = ChainLink(
            chainLinkListItem.id,
            chainLinkListItem.name,
            chainLinkListItem.password,
            chainLinkListItem.chainId
        )

        send(WebSocket.Message(Json.encodeToString(chainLink), WebSocket.MessageType.UPDATE_CHAIN_LINK).toFrame())
        close()
    }

    suspend fun delete(chainLinkListItem: ChainLinkListItem) = httpClient.webSocket {
        val chainLink = ChainLink(
            chainLinkListItem.id,
            chainLinkListItem.name,
            chainLinkListItem.password,
            chainLinkListItem.chainId
        )

        send(WebSocket.Message(Json.encodeToString(chainLink), WebSocket.MessageType.DELETE_CHAIN_LINK).toFrame())
        close()
    }
}
