package io.sunland.chainpass.common.repository

import io.ktor.client.*
import io.rsocket.kotlin.ktor.client.rSocket
import io.sunland.chainpass.common.Settings
import io.sunland.chainpass.common.network.Payload
import io.sunland.chainpass.common.network.PayloadRoute
import io.sunland.chainpass.common.network.decodeFromPayload
import io.sunland.chainpass.common.network.encodeToPayload
import kotlinx.serialization.json.Json

class ChainNetRepository(private val httpClient: HttpClient, private val settings: Settings) : ChainRepository {
    override suspend fun create(chainEntity: ChainEntity) = runCatching {
        httpClient.rSocket(settings.serverHost, settings.serverPort)
            .requestResponse(Json.encodeToPayload(PayloadRoute.CHAIN_CREATE, chainEntity)).close()
    }

    override suspend fun read() = runCatching {
        val payload = httpClient.rSocket(settings.serverHost, settings.serverPort)
            .requestResponse(Payload(PayloadRoute.CHAIN_READ))

        Json.decodeFromPayload<List<ChainEntity>>(payload)
    }

    override suspend fun read(id: Int): Result<ChainEntity> {
        throw NotImplementedError("Not yet implemented")
    }

    override suspend fun delete(chainKeyEntity: ChainKeyEntity) = runCatching {
        httpClient.rSocket(settings.serverHost, settings.serverPort)
            .requestResponse(Json.encodeToPayload(PayloadRoute.CHAIN_DELETE, chainKeyEntity)).close()
    }

    override suspend fun key(id: Int) = runCatching {
        val payload = httpClient.rSocket(settings.serverHost, settings.serverPort)
            .requestResponse(Json.encodeToPayload(PayloadRoute.CHAIN_KEY, id))

        Json.decodeFromPayload<ChainKeyEntity>(payload)
    }
}