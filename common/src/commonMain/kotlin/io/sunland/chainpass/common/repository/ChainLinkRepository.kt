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
    fun create(chainLinkEntity: ChainLinkEntity) = database.chainLinkQueries.transaction {
        database.chainLinkQueries.insert(
            chainLinkEntity.id,
            chainLinkEntity.name,
            chainLinkEntity.description,
            chainLinkEntity.password,
            chainLinkEntity.chainId
        )
    }

    fun getBy(chainId: String) = database.chainLinkQueries.transactionWithResult {
        database.chainLinkQueries.selectBy(chainId).executeAsList().map { record ->
            ChainLinkEntity(record.id, record.name, record.description, record.password, record.chainId)
        }
    }

    fun getOne(id: String) = runCatching {
        database.chainLinkQueries.transactionWithResult {
            val record = database.chainLinkQueries.selectOne(id).executeAsOne()

            ChainLinkEntity(record.id, record.name, record.description, record.password, record.chainId)
        }
    }

    fun update(chainLinkEntity: ChainLinkEntity) = database.chainLinkQueries.transaction {
        database.chainLinkQueries.update(chainLinkEntity.description, chainLinkEntity.password, chainLinkEntity.id)
    }

    fun delete(chainLinkEntity: ChainLinkEntity) = database.chainLinkQueries.transaction {
        database.chainLinkQueries.delete(chainLinkEntity.id, chainLinkEntity.chainId)
    }
}