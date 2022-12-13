package io.sunland.chainpass.server.repository

import io.sunland.chainpass.common.network.ChainKeyEntity
import io.sunland.chainpass.common.network.ChainLinkEntity
import io.sunland.chainpass.server.ChainLinkTable
import io.sunland.chainpass.server.Database
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

object ChainLinkRepository {
    suspend fun create(chainLinkEntity: ChainLinkEntity) = runCatching {
        Database.execute<Unit> {
            ChainLinkTable.insert { statement ->
                statement[id] = chainLinkEntity.id
                statement[name] = chainLinkEntity.name
                statement[description] = chainLinkEntity.description
                statement[password] = chainLinkEntity.password
                statement[chainId] = chainLinkEntity.chainKey.id
            }
        }
    }

    suspend fun read(chainKeyEntity: ChainKeyEntity) = runCatching {
        Database.execute {
            ChainLinkTable.select { ChainLinkTable.chainId eq chainKeyEntity.id }.map { record ->
                ChainLinkEntity(
                    record[ChainLinkTable.id],
                    record[ChainLinkTable.name],
                    record[ChainLinkTable.description],
                    record[ChainLinkTable.password],
                    chainKeyEntity
                )
            }
        }
    }

    suspend fun update(chainLinkEntity: ChainLinkEntity) = runCatching {
        Database.execute<Unit> {
            ChainLinkTable.update({
                (ChainLinkTable.id eq chainLinkEntity.id) and (ChainLinkTable.chainId eq chainLinkEntity.chainKey.id)
            }) { statement ->
                statement[password] = chainLinkEntity.password
                statement[description] = chainLinkEntity.description
            }
        }
    }

    suspend fun delete(chainLinkEntity: ChainLinkEntity) = runCatching {
        Database.execute<Unit> {
            ChainLinkTable.deleteWhere {
                (id eq chainLinkEntity.id) and (chainId eq chainLinkEntity.chainKey.id)
            }
        }
    }
}