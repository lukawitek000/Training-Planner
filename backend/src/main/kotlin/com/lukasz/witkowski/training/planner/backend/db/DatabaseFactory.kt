package com.lukasz.witkowski.training.planner.backend.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    fun init(
        jdbcUrl: String = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
        driverClassName: String = "org.h2.Driver"
    ) {
        val database = Database.connect(createHikariDataSource(jdbcUrl, driverClassName))
        transaction(database) {
            SchemaUtils.create(
                UsersTable,
                CategoriesTable,
                ExercisesTable,
                ExerciseCategoriesTable,
                TrainingPlansTable,
                TrainingExercisesTable
            )
        }
    }

    private fun createHikariDataSource(jdbcUrl: String, driverClassName: String): HikariDataSource {
        val config = HikariConfig().apply {
            this.driverClassName = driverClassName
            this.jdbcUrl = jdbcUrl
            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
        return HikariDataSource(config)
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T {
        val currentTransaction = TransactionManager.currentOrNull()
        return if (currentTransaction != null) {
            block()
        } else {
            newSuspendedTransaction(Dispatchers.IO) { block() }
        }
    }
}
