package io.github.oxiadenine.chainpass.repository

import io.github.oxiadenine.chainpass.ChainTable
import io.github.oxiadenine.chainpass.Database
import io.github.oxiadenine.chainpass.Storage
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll

@Serializable
data class ChainEntity(
    val id: String,
    val name: String,
    val key: String,
    val salt: String,
)

class ChainRepository(private val database: Database, val storage: Storage) {
    suspend fun create(chainEntity: ChainEntity) = database.transaction {
        ChainTable.insert { statement ->
            statement[id] = chainEntity.id
            statement[name] = chainEntity.name
            statement[key] = chainEntity.key
            statement[salt] = chainEntity.salt
        }

        Unit
    }

    suspend fun getAll() = database.transaction {
        ChainTable.selectAll().map { record ->
            ChainEntity(
                record[ChainTable.id],
                record[ChainTable.name],
                record[ChainTable.key],
                record[ChainTable.salt]
            )
        }
    }

    suspend fun getOne(id: String) = runCatching {
        database.transaction {
            val record = ChainTable.select { ChainTable.id eq id }.first()

            ChainEntity(
                record[ChainTable.id],
                record[ChainTable.name],
                record[ChainTable.key],
                record[ChainTable.salt]
            )
        }
    }

    suspend fun delete(id: String) = database.transaction {
        ChainTable.deleteWhere { ChainTable.id eq id }

        Unit
    }
}