package io.sunland.chainpass.common.repository

import kotlinx.serialization.Serializable

@Serializable
data class ChainLinkEntity(var id: Int, val name: String, val password: String, val chainId: Int)

interface ChainLinkRepository {
    suspend fun create(chainLinkEntity: ChainLinkEntity): Result<Int>
    suspend fun read(chainEntity: ChainEntity): Result<List<ChainLinkEntity>>
    suspend fun update(chainLinkEntity: ChainLinkEntity): Result<Unit>
    suspend fun delete(chainLinkEntity: ChainLinkEntity): Result<Unit>
}
