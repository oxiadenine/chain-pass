package io.sunland.chainpass.common

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection

object ChainTable : Table("chain") {
    val id = text("id")
    val name = text("name").index()
    val key = text("key")

    override val primaryKey = PrimaryKey(id)
}

object ChainLinkTable : Table("chain_link") {
    val id = text("id")
    val name = text("name").index()
    val description = text("description").index()
    val password = text("password")
    val chainId = text("chain_id").references(ChainTable.id, onDelete = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(id, chainId)
}

class Database private constructor(private val connection: Database) {
    companion object {
        fun create(dataDir: String): io.sunland.chainpass.common.Database {
            val connection = Database.connect(
                url = "jdbc:h2:$dataDir/chain_pass;FILE_LOCK=FS",
                driver = "org.h2.Driver",
                user = "chain_pass",
                password = "chain_pass",
                setupConnection = { connection ->
                    connection.autoCommit = true
                    connection.transactionIsolation = Connection.TRANSACTION_REPEATABLE_READ
                }
            )

            return Database(connection)
        }
    }

    init {
        transaction(connection) {
            SchemaUtils.create(ChainTable, ChainLinkTable)
        }
    }

    suspend fun <T> transaction(statement: suspend Transaction.() -> T) = newSuspendedTransaction(
        context = Dispatchers.IO,
        db = connection,
        transactionIsolation = Connection.TRANSACTION_REPEATABLE_READ,
        statement = statement
    )
}