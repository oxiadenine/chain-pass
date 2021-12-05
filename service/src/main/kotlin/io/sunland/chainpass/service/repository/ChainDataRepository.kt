package io.sunland.chainpass.service.repository

import io.sunland.chainpass.common.repository.ChainEntity
import io.sunland.chainpass.common.repository.ChainKeyEntity
import io.sunland.chainpass.common.repository.ChainRepository
import io.sunland.chainpass.service.ChainTable
import io.sunland.chainpass.service.Database
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import java.security.SecureRandom
import java.util.*

object ChainDataRepository : ChainRepository {
    private val seeds = Collections.synchronizedList<String>(mutableListOf())

    override suspend fun create(chainEntity: ChainEntity) = runCatching {
        Database.execute {
            ChainTable.insertAndGetId { statement ->
                statement[name] = chainEntity.name
                statement[key] = chainEntity.key
            }.value
        }
    }

    override suspend fun read() = runCatching {
        Database.execute {
            ChainTable.selectAll().map { record ->
                ChainEntity(record[ChainTable.id].value, record[ChainTable.name])
            }
        }
    }

    override suspend fun read(id: Int) = runCatching {
        Database.execute {
            val record = ChainTable.select { ChainTable.id eq id }.first()

            ChainEntity(record[ChainTable.id].value, record[ChainTable.name], record[ChainTable.key])
        }
    }

    override suspend fun delete(chainKeyEntity: ChainKeyEntity) = runCatching {
        Database.execute<Unit> {
            ChainTable.deleteWhere { ChainTable.id eq chainKeyEntity.id }
        }
    }

    override suspend fun seed(saved: Boolean) = runCatching {
        if (saved) {
            seeds.removeAt(0)
        } else {
            val seedBytes = ByteArray(16)

            SecureRandom().nextBytes(seedBytes)

            val seed = Base64.getEncoder().encodeToString(seedBytes)

            seeds.add(seed)

            seed
        }
    }
}
