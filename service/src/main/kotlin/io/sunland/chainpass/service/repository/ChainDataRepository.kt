package io.sunland.chainpass.service.repository

import io.sunland.chainpass.common.repository.Chain
import io.sunland.chainpass.common.repository.ChainRepository
import io.sunland.chainpass.service.ChainTable
import io.sunland.chainpass.service.Database
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll

object ChainDataRepository : ChainRepository {
    override suspend fun create(chain: Chain) = runCatching {
        Database.execute {
            ChainTable.insertAndGetId { statement ->
                statement[name] = chain.name
                statement[key] = chain.key
            }.value
        }
    }

    override suspend fun read() = runCatching {
        Database.execute {
            ChainTable.selectAll().map { record ->
                Chain(record[ChainTable.id].value, record[ChainTable.name], record[ChainTable.key])
            }
        }
    }

    override suspend fun delete(chain: Chain) = runCatching {
        Database.execute<Unit> {
            ChainTable.deleteWhere { ChainTable.id eq chain.id }
        }
    }
}
