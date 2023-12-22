package com.github.sunland.chainpass.repository

import com.github.sunland.chainpass.ChainLinkTable
import com.github.sunland.chainpass.Database
import com.github.sunland.chainpass.Storage
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

@Serializable
data class ChainLinkEntity(
    var id: String,
    val name: String,
    val description: String,
    val password: String,
    val iv: String,
    val chainId: String
)

class ChainLinkRepository(private val database: Database, val storage: Storage) {
    suspend fun create(chainLinkEntity: ChainLinkEntity) = database.transaction {
        ChainLinkTable.insert { statement ->
            statement[id] = chainLinkEntity.id
            statement[name] = chainLinkEntity.name
            statement[description] = chainLinkEntity.description
            statement[password] = chainLinkEntity.password
            statement[iv] = chainLinkEntity.iv
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
                record[ChainLinkTable.iv],
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
                record[ChainLinkTable.iv],
                record[ChainLinkTable.chainId]
            )
        }
    }

    suspend fun update(chainLinkEntity: ChainLinkEntity) = database.transaction {
        ChainLinkTable.update({ ChainLinkTable.id eq chainLinkEntity.id }) { statement ->
            statement[description] = chainLinkEntity.description
            statement[password] = chainLinkEntity.password
            statement[iv] = chainLinkEntity.iv
        }

         Unit
    }

    suspend fun delete(chainLinkEntity: ChainLinkEntity) = database.transaction {
        ChainLinkTable.deleteWhere { (id eq chainLinkEntity.id) and (chainId eq chainLinkEntity.chainId) }

        Unit
    }
}