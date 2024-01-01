package io.sunland.chainpass.common.network

import io.rsocket.kotlin.RSocket
import io.rsocket.kotlin.payload.Payload
import io.sunland.chainpass.common.repository.ChainLinkEntity
import io.sunland.chainpass.common.repository.ChainLinkRepository

class ChainLinkApi(private val chainLinkRepository: ChainLinkRepository, private val tcpSocket: RSocket) {
    suspend fun sync(chainId: String) = runCatching {
        val chainLinkEntities = tcpSocket.requestResponse(
            Payload.encode(WebSocket.Route.CHAIN_LINK_SYNC, chainId)
        ).decode<List<ChainLinkEntity>>()

        chainLinkEntities.forEach { chainLinkEntity ->
            chainLinkRepository.getOne(chainLinkEntity.id).onSuccess { chainLinkEntityFound ->
                if (chainLinkEntity.password != chainLinkEntityFound.password
                    || chainLinkEntity.description != chainLinkEntityFound.description) {
                    chainLinkRepository.update(chainLinkEntity).getOrThrow()
                }
            }.onFailure {
                chainLinkRepository.create(chainLinkEntity).getOrThrow()
            }
        }
    }
}