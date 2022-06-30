package io.sunland.chainpass.server.repository

import io.sunland.chainpass.common.repository.ChainKeyEntity
import io.sunland.chainpass.common.repository.ChainLinkEntity
import io.sunland.chainpass.common.repository.ChainLinkRepository
import io.sunland.chainpass.server.ChainLinkTable
import io.sunland.chainpass.server.Database
import org.jetbrains.exposed.sql.*

object ChainLinkDataRepository : ChainLinkRepository {
    override suspend fun create(chainLinkEntity: ChainLinkEntity) = runCatching {
        Database.execute {
            ChainLinkTable.insertAndGetId { statement ->
                statement[name] = chainLinkEntity.name
                statement[description] = chainLinkEntity.description
                statement[password] = chainLinkEntity.password
                statement[chainId] = chainLinkEntity.chainKey.id
            }.value
        }
    }

    override suspend fun read(chainKeyEntity: ChainKeyEntity) = runCatching {
        Database.execute {
            ChainLinkTable.select { ChainLinkTable.chainId eq chainKeyEntity.id }.map { record ->
                ChainLinkEntity(
                    record[ChainLinkTable.id].value,
                    record[ChainLinkTable.name],
                    record[ChainLinkTable.description],
                    record[ChainLinkTable.password],
                    chainKeyEntity
                )
            }
        }
    }

    override suspend fun update(chainLinkEntity: ChainLinkEntity) = runCatching {
        Database.execute<Unit> {
            ChainLinkTable.update({
                (ChainLinkTable.id eq chainLinkEntity.id) and (ChainLinkTable.chainId eq chainLinkEntity.chainKey.id)
            }) { statement ->
                statement[password] = chainLinkEntity.password
                statement[description] = chainLinkEntity.description
            }
        }
    }

    override suspend fun delete(chainLinkEntity: ChainLinkEntity) = runCatching {
        Database.execute<Unit> {
            ChainLinkTable.deleteWhere {
                (ChainLinkTable.id eq chainLinkEntity.id) and (ChainLinkTable.chainId eq chainLinkEntity.chainKey.id)
            }
        }
    }
}