package io.sunland.chainpass.common.repository

import kotlinx.serialization.Serializable

@Serializable
data class ChainEntity(var id: Int, val name: String, val key: String = "")

@Serializable
data class ChainKeyEntity(val id: Int, val key: String)

interface ChainRepository {
    suspend fun create(chainEntity: ChainEntity): Result<Int>
    suspend fun read(): Result<List<ChainEntity>>
    suspend fun read(id: Int): Result<ChainEntity>
    suspend fun delete(chainKeyEntity: ChainKeyEntity): Result<Unit>
    suspend fun key(id: Int): Result<ChainKeyEntity>
}
