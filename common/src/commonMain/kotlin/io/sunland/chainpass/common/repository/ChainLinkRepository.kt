package io.sunland.chainpass.common.repository

import io.sunland.chainpass.common.*
import io.sunland.chainpass.common.Database
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

@Serializable
data class ChainLinkEntity(
    var id: String,
    val name: String,
    val description: String,
    val password: String,
    val chainId: String
)

class ChainLinkRepository(private val database: Database, private val storage: Storage) {
    suspend fun create(chainLinkEntity: ChainLinkEntity) = database.transaction {
        ChainLinkTable.insert { statement ->
            statement[id] = chainLinkEntity.id
            statement[name] = chainLinkEntity.name
            statement[description] = chainLinkEntity.description
            statement[password] = chainLinkEntity.password
            statement[chainId] = chainLinkEntity.chainId
        }

         Unit
    }

    suspend fun getBy(chainId: String) = database.transaction {
        ChainLinkTable.select { ChainLinkTable.chainId eq chainId }.map { record ->
            ChainLinkEntity(
                record[ChainLinkTable.id],
                record[ChainLinkTable.name],
                record[ChainLinkTable.description],
                record[ChainLinkTable.password],
                record[ChainLinkTable.chainId]
            )
        }
    }

    suspend fun getOne(id: String) = runCatching {
        database.transaction {
            val record = ChainLinkTable.select { ChainLinkTable.id eq id }.first()

            ChainLinkEntity(
                record[ChainLinkTable.id],
                record[ChainLinkTable.name],
                record[ChainLinkTable.description],
                record[ChainLinkTable.password],
                record[ChainLinkTable.chainId]
            )
        }
    }

    suspend fun update(chainLinkEntity: ChainLinkEntity) = database.transaction {
        ChainLinkTable.update({ ChainLinkTable.id eq chainLinkEntity.id }) { statement ->
            statement[description] = chainLinkEntity.description
            statement[password] = chainLinkEntity.password
        }

         Unit
    }

    suspend fun delete(chainLinkEntity: ChainLinkEntity) = database.transaction {
        ChainLinkTable.deleteWhere { (id eq chainLinkEntity.id) and (chainId eq chainLinkEntity.chainId) }

        Unit
    }

    fun store(storageType: StorageType, storable: Storable) = storage.store(storageType, storable)

    fun unstore(filePath: String, fileBytes: ByteArray) = storage.unstore(filePath, fileBytes)
}