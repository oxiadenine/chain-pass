package io.sunland.chainpass.common

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import io.sunland.chainpass.sqldelight.Database

actual class DriverFactory(private val dirPath: String) {
    actual fun createDriver(): SqlDriver {
        val driver: SqlDriver = JdbcSqliteDriver("jdbc:sqlite:$dirPath/chain_pass.db")

        Database.Schema.create(driver)

        return driver
    }
}