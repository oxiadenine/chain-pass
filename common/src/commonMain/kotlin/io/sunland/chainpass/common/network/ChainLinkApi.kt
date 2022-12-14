package io.sunland.chainpass.common.network

import io.ktor.client.*
import io.rsocket.kotlin.ktor.client.rSocket
import io.rsocket.kotlin.payload.Payload
import io.sunland.chainpass.common.Settings
import io.sunland.chainpass.common.network.WebSocket.decode
import io.sunland.chainpass.common.network.WebSocket.encode
import kotlinx.serialization.Serializable

@Serializable
data class ChainLinkEntity(
    var id: Int,
    val name: String,
    val description: String,
    val password: String,
    val chainKey: ChainKeyEntity
)

class ChainLinkApi(private val httpClient: HttpClient, private val settings: Settings) {
    suspend fun create(chainLinkEntities: List<ChainLinkEntity>) = runCatching {
        httpClient.rSocket(settings.serverHost, settings.serverPort).requestResponse(
            Payload.encode(WebSocket.Route.CHAIN_LINK_CREATE, chainLinkEntities)
        ).close()
    }

    suspend fun read(chainKeyEntity: ChainKeyEntity) = runCatching {
        httpClient.rSocket(settings.serverHost, settings.serverPort).requestResponse(
            Payload.encode(WebSocket.Route.CHAIN_LINK_READ, chainKeyEntity)
        ).decode<List<ChainLinkEntity>>()
    }

    suspend fun update(chainLinkEntity: ChainLinkEntity) = runCatching {
        httpClient.rSocket(settings.serverHost, settings.serverPort).requestResponse(
            Payload.encode(WebSocket.Route.CHAIN_LINK_UPDATE, chainLinkEntity)
        ).close()
    }

    suspend fun delete(chainLinkEntity: ChainLinkEntity) = runCatching {
        httpClient.rSocket(settings.serverHost, settings.serverPort).requestResponse(
            Payload.encode(WebSocket.Route.CHAIN_LINK_DELETE, chainLinkEntity)
        ).close()
    }
}