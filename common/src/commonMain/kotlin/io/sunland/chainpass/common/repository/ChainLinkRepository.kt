package io.sunland.chainpass.common.repository

import kotlinx.serialization.Serializable

@Serializable
data class ChainLinkEntity(
    var id: Int,
    val name: String,
    val description: String,
    val password: String,
    val chainKey: ChainKeyEntity
)

interface ChainLinkRepository {
    suspend fun create(chainLinkEntity: ChainLinkEntity): Result<Int>
    suspend fun read(chainKeyEntity: ChainKeyEntity): Result<List<ChainLinkEntity>>
    suspend fun update(chainLinkEntity: ChainLinkEntity): Result<Unit>
    suspend fun delete(chainLinkEntity: ChainLinkEntity): Result<Unit>
}