package com.github.sunland.chainpass.network

import io.rsocket.kotlin.RSocket
import io.rsocket.kotlin.payload.Payload
import com.github.sunland.chainpass.repository.ChainLinkEntity
import com.github.sunland.chainpass.repository.ChainLinkRepository

class ChainLinkApi(private val chainLinkRepository: ChainLinkRepository, private val tcpSocket: RSocket) {
    suspend fun sync(chainId: String) = runCatching {
        val chainLinkEntities = tcpSocket.requestResponse(
            Payload.encode(WebSocket.Route.CHAIN_LINK_SYNC, chainId)
        ).decode<List<ChainLinkEntity>>()

        chainLinkEntities.forEach { chainLinkEntity ->
            chainLinkRepository.getOne(chainLinkEntity.id).onSuccess { chainLinkEntityFound ->
                if (chainLinkEntity.password != chainLinkEntityFound.password
                    || chainLinkEntity.description != chainLinkEntityFound.description) {
                    chainLinkRepository.update(chainLinkEntity)
                }
            }.onFailure {
                chainLinkRepository.create(chainLinkEntity)
            }
        }
    }
}