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
            ChainLinkTable.insertAndGetId { statement ->
                statement[name] = chainLink.name
                statement[password] = chainLink.password
                statement[chainId] = chainLink.chainId
            }.value
        }
    }

    override suspend fun read(chain: Chain) = runCatching {
        Database.execute {
            ChainLinkTable.select { ChainLinkTable.chainId eq chain.id }.map { record ->
                ChainLink(
                    record[ChainLinkTable.id].value,
                    record[ChainLinkTable.name],
                    record[ChainLinkTable.password],
                    record[ChainLinkTable.chainId].value
                )
            }
        }
    }

    override suspend fun update(chainLink: ChainLink) = runCatching {
        Database.execute<Unit> {
            ChainLinkTable.update({ ChainLinkTable.id eq chainLink.id }) { statement ->
                statement[password] = chainLink.password
            }
        }
    }

    override suspend fun delete(chainLink: ChainLink) = runCatching {
        Database.execute<Unit> {
            ChainLinkTable.deleteWhere { ChainLinkTable.id eq chainLink.id }
        }
    }
}
