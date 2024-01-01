package io.sunland.chainpass.service.repository

import io.sunland.chainpass.common.repository.Chain
import io.sunland.chainpass.common.repository.ChainRepository
import io.sunland.chainpass.service.ChainTable
import io.sunland.chainpass.service.Database
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll

object ChainDataRepository : ChainRepository {
    override suspend fun create(chain: Chain) = runCatching {
        Database.execute {
            ChainTable.insertAndGetId {
                it[name] = chain.name
                it[key] = chain.key
            }.value
        }
    }

    override suspend fun read() = runCatching {
        Database.execute {
            ChainTable.selectAll().map {
                Chain(it[ChainTable.id].value, it[ChainTable.name], it[ChainTable.key])
            }
        }
    }

    override suspend fun delete(chain: Chain) = runCatching {
        Database.execute<Unit> {
            ChainTable.deleteWhere {
                (ChainTable.id eq chain.id) and (ChainTable.name eq chain.name)
            }
        }
    }
}
