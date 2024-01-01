package io.sunland.chainpass.common.repository

import io.sunland.chainpass.common.*
import io.sunland.chainpass.sqldelight.Database
import kotlinx.serialization.Serializable

@Serializable
data class ChainEntity(val id: String, val name: String, val key: String = "")

class ChainRepository(private val database: Database, private val storage: Storage) {
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

    fun store(storageType: StorageType, storable: Storable) = storage.store(storageType, storable)

    fun unstore(filePath: String) = storage.unstore(filePath)
}