package io.sunland.chainpass.common.network

import io.rsocket.kotlin.RSocket
import io.rsocket.kotlin.payload.Payload
import io.sunland.chainpass.common.repository.ChainEntity
import io.sunland.chainpass.common.repository.ChainLinkEntity
import io.sunland.chainpass.common.repository.ChainLinkRepository
import io.sunland.chainpass.common.repository.ChainRepository

class ChainApi(
    private val chainRepository: ChainRepository,
    private val chainLinkRepository: ChainLinkRepository,
    private val tcpSocket: RSocket
) {
    suspend fun sync() = runCatching {
        val chainEntities = tcpSocket.requestResponse(
            Payload.encode(WebSocket.Route.CHAIN_SYNC)
        ).decode<List<ChainEntity>>()

        chainEntities.forEach { chainEntity ->
            chainRepository.getOne(chainEntity.id).onFailure {
                chainRepository.create(chainEntity)
            }

            val chainLinkEntities = tcpSocket.requestResponse(
                Payload.encode(WebSocket.Route.CHAIN_LINK_SYNC, chainEntity.id)
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
}