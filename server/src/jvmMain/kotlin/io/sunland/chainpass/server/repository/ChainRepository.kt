package io.sunland.chainpass.server.repository

import io.sunland.chainpass.common.network.ChainEntity
import io.sunland.chainpass.common.network.ChainKeyEntity
import io.sunland.chainpass.common.security.PasswordEncoder
import io.sunland.chainpass.server.ChainLinkTable
import io.sunland.chainpass.server.ChainTable
import io.sunland.chainpass.server.Database
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import java.security.SecureRandom
import java.util.*

object ChainRepository {
    private val keys = Collections.synchronizedList<ChainKeyEntity>(mutableListOf())

    suspend fun create(chainEntity: ChainEntity) = runCatching {
        Database.execute<Unit> {
            ChainTable.insert { statement ->
                statement[id] = chainEntity.id
                statement[name] = chainEntity.name
                statement[key] = chainEntity.key
            }
        }
    }

    suspend fun read() = runCatching {
        Database.execute {
            ChainTable.selectAll().map { record ->
                ChainEntity(record[ChainTable.id], record[ChainTable.name])
            }
        }
    }

    suspend fun read(id: Int) = runCatching {
        Database.execute {
            val record = ChainTable.select { ChainTable.id eq id }.first()

            ChainEntity(record[ChainTable.id], record[ChainTable.name], record[ChainTable.key])
        }
    }

    suspend fun delete(chainKeyEntity: ChainKeyEntity) = runCatching {
        Database.execute<Unit> {
            ChainLinkTable.deleteWhere { chainId eq chainKeyEntity.id }
            ChainTable.deleteWhere { id eq chainKeyEntity.id }
        }
    }

    fun key(id: Int) = runCatching {
        var key = keys.firstOrNull { key -> key.id == id }

        if (key == null) {
            val salt = ByteArray(16)

            SecureRandom().nextBytes(salt)

            key = ChainKeyEntity(id, PasswordEncoder.Base64.encode(salt))

            keys.add(key)
        } else keys.remove(key)

        key
    }
}