package io.sunland.chainpass.common.repository

import io.sunland.chainpass.sqldelight.Database
import kotlinx.serialization.Serializable

@Serializable
data class ChainLinkEntity(
    var id: String,
    val name: String,
    val description: String,
    val password: String,
    val chainId: String
)

class ChainLinkRepository(private val database: Database) {
    fun create(chainLinkEntity: ChainLinkEntity) = runCatching {
        database.chainLinkQueries.transaction {
            database.chainLinkQueries.insert(
                id = chainLinkEntity.id,
                name = chainLinkEntity.name,
                description = chainLinkEntity.description,
                password = chainLinkEntity.password,
                chainId = chainLinkEntity.chainId
            )
        }
    }

    fun getAll() = runCatching {
        database.chainLinkQueries.transactionWithResult {
            database.chainLinkQueries.selectAll().executeAsList().map { record ->
                ChainLinkEntity(record.id, record.name, record.description, record.password, record.chainId)
            }
        }
    }

    fun getBy(chainId: String) = runCatching {
        database.chainLinkQueries.transactionWithResult {
            database.chainLinkQueries.selectBy(chainId).executeAsList().map { record ->
                ChainLinkEntity(record.id, record.name, record.description, record.password, record.chainId)
            }
        }
    }

    fun getOne(id: String) = runCatching {
        database.chainLinkQueries.transactionWithResult {
            val record = database.chainLinkQueries.selectOne(id).executeAsOne()

            ChainLinkEntity(record.id, record.name, record.description, record.password, record.chainId)
        }
    }

    fun update(chainLinkEntity: ChainLinkEntity) = runCatching {
        database.chainLinkQueries.transaction {
            database.chainLinkQueries.update(
                id = chainLinkEntity.id,
                description = chainLinkEntity.description,
                password = chainLinkEntity.password
            )
        }
    }

    fun delete(chainLinkEntity: ChainLinkEntity) = runCatching {
        database.chainLinkQueries.transaction {
            database.chainLinkQueries.delete(id = chainLinkEntity.id, chainId = chainLinkEntity.chainId)
        }
    }
}