package io.sunland.chainpass.service.repository

import io.sunland.chainpass.common.repository.Chain
import io.sunland.chainpass.common.repository.ChainLink
import io.sunland.chainpass.common.repository.ChainLinkRepository
import io.sunland.chainpass.service.ChainLinkTable
import io.sunland.chainpass.service.Database
import org.jetbrains.exposed.sql.*

object ChainLinkDataRepository : ChainLinkRepository {
    override suspend fun create(chainLink: ChainLink) = runCatching {
        Database.execute {
            ChainLinkTable.insertAndGetId {
                it[name] = chainLink.name
                it[password] = chainLink.password
                it[chainId] = chainLink.chainId
            }.value
        }
    }

    override suspend fun read(chain: Chain) = runCatching {
        Database.execute {
            ChainLinkTable.select { ChainLinkTable.chainId eq chain.id }.map {
                ChainLink(
                    it[ChainLinkTable.id].value,
                    it[ChainLinkTable.name],
                    it[ChainLinkTable.password],
                    it[ChainLinkTable.chainId].value
                )
            }
        }
    }

    override suspend fun update(chainLink: ChainLink) = runCatching {
        Database.execute<Unit> {
            ChainLinkTable.update({ ChainLinkTable.id eq chainLink.id }) {
                it[password] = chainLink.password
            }
        }
    }

    override suspend fun delete(chainLink: ChainLink) = runCatching {
        Database.execute<Unit> {
            ChainLinkTable.deleteWhere { ChainLinkTable.id eq chainLink.id }
        }
    }
}
