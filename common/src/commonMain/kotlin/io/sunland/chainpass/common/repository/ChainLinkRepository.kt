package io.sunland.chainpass.common.repository

import kotlinx.serialization.Serializable

@Serializable
data class ChainLink(var id: Int, val name: String, val password: String, val chainId: Int)

interface ChainLinkRepository {
    suspend fun create(chainLink: ChainLink): Int
    suspend fun read(chain: Chain): List<ChainLink>
    suspend fun update(chainLink: ChainLink)
    suspend fun delete(chainLink: ChainLink)
}
