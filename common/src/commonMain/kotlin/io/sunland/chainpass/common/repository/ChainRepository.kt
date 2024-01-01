package io.sunland.chainpass.common.repository

import io.sunland.chainpass.sqldelight.Database
import kotlinx.serialization.Serializable

@Serializable
data class ChainEntity(val id: String, val name: String, val key: String = "")

class ChainRepository(private val database: Database) {
    fun create(chainEntity: ChainEntity) = runCatching {
        database.chainQueries.transaction {
            database.chainQueries.insert(
                id = chainEntity.id,
                name = chainEntity.name,
                key = chainEntity.key
            )
        }
    }

    fun getAll() = runCatching {
        database.chainQueries.transactionWithResult {
            database.chainQueries.selectAll().executeAsList().map { record ->
                ChainEntity(record.id, record.name, record.key)
            }
        }
    }

    fun getOne(id: String) = runCatching {
        database.chainQueries.transactionWithResult {
            val record = database.chainQueries.selectOne(id).executeAsOne()

            ChainEntity(record.id, record.name, record.key)
        }
    }

    fun delete(id: String) = runCatching {
        database.chainQueries.transaction {
            database.chainQueries.delete(id)
        }
    }
}