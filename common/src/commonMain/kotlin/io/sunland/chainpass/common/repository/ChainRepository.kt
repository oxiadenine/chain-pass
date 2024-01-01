package io.sunland.chainpass.common.repository

import io.sunland.chainpass.common.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll

@Serializable
data class ChainEntity(val id: String, val name: String, val key: String = "")

class ChainRepository(private val database: Database, private val storage: Storage) {
    suspend fun create(chainEntity: ChainEntity) = database.transaction {
        ChainTable.insert { statement ->
            statement[id] = chainEntity.id
            statement[name] = chainEntity.name
            statement[key] = chainEntity.key
        }

        Unit
    }

    suspend fun getAll() = database.transaction {
        ChainTable.selectAll().map { record ->
            ChainEntity(record[ChainTable.id], record[ChainTable.name], record[ChainTable.key])
        }
    }

    suspend fun getOne(id: String) = runCatching {
        database.transaction {
            val record = ChainTable.select { ChainTable.id eq id }.first()

            ChainEntity(record[ChainTable.id], record[ChainTable.name], record[ChainTable.key])
        }
    }

    suspend fun delete(id: String) = database.transaction {
        ChainTable.deleteWhere { ChainTable.id eq id }

        Unit
    }

    fun store(storageType: StorageType, storable: Storable) = storage.store(storageType, storable)

    fun unstore(filePath: String) = storage.unstore(filePath)
}