package io.sunland.chainpass.common

import com.squareup.sqldelight.db.SqlDriver
import io.sunland.chainpass.sqldelight.Database

object DatabaseFactory {
    fun createDatabase(driverFactory: DriverFactory): Database {
        val driver = driverFactory.createDriver()

        return Database(driver)
    }
}

expect class DriverFactory {
    fun createDriver(): SqlDriver
}