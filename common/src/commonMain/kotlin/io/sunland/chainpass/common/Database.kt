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
    val salt = text("salt")

    override val primaryKey = PrimaryKey(id)
}

object ChainLinkTable : Table("chain_link") {
    val id = text("id")
    val name = text("name").index()
    val description = text("description").index()
    val password = text("password")
    val iv = text("iv")

    val chainId = text("chain_id").references(ChainTable.id, onDelete = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(id, chainId)
}

class Database private constructor(private val connection: Database) {
    companion object {
        fun create(dataDirPath: String): io.sunland.chainpass.common.Database {
            val connection = Database.connect(
                url = "jdbc:h2:$dataDirPath/chain_pass;FILE_LOCK=FS",
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
            if (platform == Platform.ANDROID) {
                val tables = SchemaUtils.listTables().filter { table ->
                    table.contains("public", ignoreCase = true)
                }

                if (tables.isEmpty()) {
                    SchemaUtils.create(ChainTable, ChainLinkTable)
                }
            } else SchemaUtils.create(ChainTable, ChainLinkTable)
        }
    }

    suspend fun <T> transaction(statement: suspend Transaction.() -> T) = newSuspendedTransaction(
        context = Dispatchers.IO,
        db = connection,
        transactionIsolation = Connection.TRANSACTION_REPEATABLE_READ,
        statement = statement
    )
}