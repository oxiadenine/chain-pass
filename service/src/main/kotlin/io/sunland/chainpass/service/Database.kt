package io.sunland.chainpass.service

import com.typesafe.config.Config
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object Database {
    private lateinit var connection: Database

    fun connect(config: Config) {
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = config.getString("url")
            driverClassName = config.getString("driver")
            username = config.getString("username")
            password = config.getString("password")

            validate()
        }

        connection = Database.connect(HikariDataSource(hikariConfig))

        transaction(connection) {
            SchemaUtils.createDatabase()
        }
    }

    suspend fun <T> execute(block: suspend () -> T) = newSuspendedTransaction(Dispatchers.IO, connection) { block() }
}
