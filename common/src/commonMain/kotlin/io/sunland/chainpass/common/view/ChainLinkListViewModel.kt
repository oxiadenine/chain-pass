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
        val socketId = call.request.headers["Socket-Id"]!!

        val chainLink = ChainLink(
            chainLinkListItem.id,
            chainLinkListItem.name,
            chainLinkListItem.password,
            chainLinkListItem.chainId
        )

        send(SocketMessage(SocketMessageType.CREATE_CHAIN_LINK, socketId, Json.encodeToString(chainLink)).toFrame())

        while (true) {
            val frame = incoming.receive() as? Frame.Text ?: continue

            val message = SocketMessage.from(frame)

            if (message.type == SocketMessageType.CREATE_CHAIN_LINK) {
                chainLinkListItem.id = Json.decodeFromString<ChainLink>(message.text).id
            }

            break
        }

        close()
    }

    suspend fun read() = httpClient.webSocket {
        val socketId = call.request.headers["Socket-Id"]!!

        val chain = Chain(chainListItem!!.id, chainListItem!!.name, chainListItem!!.key)

        send(SocketMessage(SocketMessageType.READ_CHAIN_LINK, socketId, Json.encodeToString(chain)).toFrame())

        while (true) {
            val frame = incoming.receive() as? Frame.Text ?: continue

            val message = SocketMessage.from(frame)

            if (message.type == SocketMessageType.READ_CHAIN_LINK) {
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
        val socketId = call.request.headers["Socket-Id"]!!

        val chainLink = ChainLink(
            chainLinkListItem.id,
            chainLinkListItem.name,
            chainLinkListItem.password,
            chainLinkListItem.chainId
        )

        send(SocketMessage(SocketMessageType.UPDATE_CHAIN_LINK, socketId, Json.encodeToString(chainLink)).toFrame())
        close()
    }

    suspend fun delete(chainLinkListItem: ChainLinkListItem) = httpClient.webSocket {
        val socketId = call.request.headers["Socket-Id"]!!

        val chainLink = ChainLink(
            chainLinkListItem.id,
            chainLinkListItem.name,
            chainLinkListItem.password,
            chainLinkListItem.chainId
        )

        send(SocketMessage(SocketMessageType.DELETE_CHAIN_LINK, socketId, Json.encodeToString(chainLink)).toFrame())
        close()
    }
}
