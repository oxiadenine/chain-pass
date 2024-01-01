package io.sunland.chainpass.service

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.config.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object ChainTable : IntIdTable("chain") {
    val name = varchar("name", 16).index()
    val key = varchar("key", 64)
}

object ChainLinkTable : IntIdTable("chain_link") {
    val name = varchar("name", 16).index()
    val description = varchar("description", 24).index()
    val password = varchar("password", 64)

    val chainId = reference("chain_id", ChainTable, onDelete = ReferenceOption.CASCADE).index()
}

object Database {
    private lateinit var connection: Database

    fun connect(config: ApplicationConfig) {
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = config.property("url").getString()
            driverClassName = config.property("driver").getString()
            username = config.property("username").getString()
            password = config.property("password").getString()

            validate()
        }

        connection = Database.connect(HikariDataSource(hikariConfig))

        transaction(connection) {
            SchemaUtils.create(ChainTable, ChainLinkTable)
        }
    }

    suspend fun <T> execute(block: suspend () -> T) = newSuspendedTransaction(Dispatchers.IO, connection) { block() }
}
