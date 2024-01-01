package io.sunland.chainpass.common.repository

import io.ktor.client.*
import io.rsocket.kotlin.ktor.client.rSocket
import io.sunland.chainpass.common.Settings
import io.sunland.chainpass.common.network.PayloadRoute
import io.sunland.chainpass.common.network.decodeFromPayload
import io.sunland.chainpass.common.network.encodeToPayload
import kotlinx.serialization.json.Json

class ChainLinkNetRepository(private val httpClient: HttpClient, private val settings: Settings) : ChainLinkRepository {
    override suspend fun create(chainLinkEntity: ChainLinkEntity) = runCatching {
        httpClient.rSocket(settings.serverHost, settings.serverPort)
            .requestResponse(Json.encodeToPayload(PayloadRoute.CHAIN_LINK_CREATE, chainLinkEntity)).close()
    }

    override suspend fun read(chainKeyEntity: ChainKeyEntity) = runCatching {
        val payload = httpClient.rSocket(settings.serverHost, settings.serverPort)
            .requestResponse(Json.encodeToPayload(PayloadRoute.CHAIN_LINK_READ, chainKeyEntity))

        Json.decodeFromPayload<List<ChainLinkEntity>>(payload)
    }

    override suspend fun update(chainLinkEntity: ChainLinkEntity) = runCatching {
        httpClient.rSocket(settings.serverHost, settings.serverPort)
            .requestResponse(Json.encodeToPayload(PayloadRoute.CHAIN_LINK_UPDATE, chainLinkEntity)).close()
    }

    override suspend fun delete(chainLinkEntity: ChainLinkEntity) = runCatching {
        httpClient.rSocket(settings.serverHost, settings.serverPort)
            .requestResponse(Json.encodeToPayload(PayloadRoute.CHAIN_LINK_DELETE, chainLinkEntity)).close()
    }
}