package io.sunland.chainpass.common.repository

import kotlinx.serialization.Serializable

@Serializable
data class ChainEntity(var id: Int, val name: String, val key: String)

interface ChainRepository {
    suspend fun create(chainEntity: ChainEntity): Result<Int>
    suspend fun read(): Result<List<ChainEntity>>
    suspend fun delete(chainEntity: ChainEntity): Result<Unit>
}
