package io.sunland.chainpass.common.network

import io.ktor.client.*
import io.rsocket.kotlin.ktor.client.rSocket
import io.rsocket.kotlin.payload.Payload
import io.sunland.chainpass.common.Settings
import io.sunland.chainpass.common.network.WebSocket.decode
import io.sunland.chainpass.common.network.WebSocket.encode
import kotlinx.serialization.Serializable

@Serializable
data class ChainEntity(var id: Int, val name: String, val key: String = "")

@Serializable
data class ChainKeyEntity(val id: Int, val key: String)

class ChainApi(private val httpClient: HttpClient, private val settings: Settings) {
    suspend fun create(chainEntity: ChainEntity) = runCatching {
        httpClient.rSocket(settings.serverHost, settings.serverPort).requestResponse(
            Payload.encode(WebSocket.Route.CHAIN_CREATE, chainEntity)
        ).close()
    }

    suspend fun read() = runCatching {
        httpClient.rSocket(settings.serverHost, settings.serverPort).requestResponse(
            Payload.encode(WebSocket.Route.CHAIN_READ)
        ).decode<List<ChainEntity>>()
    }

    suspend fun delete(chainKeyEntity: ChainKeyEntity) = runCatching {
        httpClient.rSocket(settings.serverHost, settings.serverPort).requestResponse(
            Payload.encode(WebSocket.Route.CHAIN_DELETE, chainKeyEntity)
        ).close()
    }

    suspend fun key(id: Int) = runCatching {
        httpClient.rSocket(settings.serverHost, settings.serverPort).requestResponse(
            Payload.encode(WebSocket.Route.CHAIN_KEY, id)
        ).decode<ChainKeyEntity>()
    }
}