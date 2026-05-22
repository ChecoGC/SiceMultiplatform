package com.example.sicemultiplatform.utils

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.example.sicemultiplatform.database.AppDatabase

class DesktopDatabaseDriverFactory : DatabaseDriverFactory {
    override fun createDriver(): SqlDriver {
        val driver = JdbcSqliteDriver("jdbc:sqlite:sice.db")
        try {
            AppDatabase.Schema.create(driver)
        } catch (e: Exception) {
            // Ignoramos si ya existe
        }
        return driver
    }
}