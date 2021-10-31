package io.sunland.chainpass.common.repository

import kotlinx.serialization.Serializable

@Serializable
data class Chain(var id: Int, val name: String, val key: String)

interface ChainRepository {
    suspend fun create(chain: Chain): Int
    suspend fun read(): List<Chain>
    suspend fun delete(chain: Chain)
}
