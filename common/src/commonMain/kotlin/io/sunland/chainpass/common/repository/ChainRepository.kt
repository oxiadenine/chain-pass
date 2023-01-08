package io.sunland.chainpass.common.repository

import io.sunland.chainpass.sqldelight.Database
import kotlinx.serialization.Serializable

@Serializable
data class ChainEntity(val id: String, val name: String, val key: String = "")

class ChainRepository(private val database: Database) {
    fun create(chainEntity: ChainEntity) = database.chainQueries.transaction {
        database.chainQueries.insert(chainEntity.id, chainEntity.name, chainEntity.key)
    }

    fun getAll() = database.chainQueries.transactionWithResult {
        database.chainQueries.selectAll().executeAsList().map { record ->
            ChainEntity(record.id, record.name, record.key)
        }
    }

    fun getOne(id: String) = runCatching {
        database.chainQueries.transactionWithResult {
            val record = database.chainQueries.selectOne(id).executeAsOne()

            ChainEntity(record.id, record.name, record.key)
        }
    }

    fun delete(id: String) = database.chainQueries.transaction {
        database.chainQueries.delete(id)
    }
}