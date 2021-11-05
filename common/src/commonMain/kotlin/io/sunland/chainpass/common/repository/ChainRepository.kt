package io.sunland.chainpass.common.repository

import kotlinx.serialization.Serializable

@Serializable
data class Chain(var id: Int, val name: String, val key: String)

interface ChainRepository {
    suspend fun create(chain: Chain): Result<Int>
    suspend fun read(): Result<List<Chain>>
    suspend fun delete(chain: Chain): Result<Unit>
}
