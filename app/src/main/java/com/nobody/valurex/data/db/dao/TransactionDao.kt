package com.nobody.valurex.data.db.dao

import androidx.room.*
import com.nobody.valurex.data.db.entity.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(transaction: Transaction): Long

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAll(): Flow<List<Transaction>>

    @Query("""
        SELECT * FROM transactions
        WHERE timestamp >= strftime('%s', date('now','start of month')) * 1000
        ORDER BY timestamp DESC
    """)
    fun getForCurrentMonth(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE categoryId = :categoryId ORDER BY timestamp DESC")
    fun getByCategory(categoryId: Long): Flow<List<Transaction>>

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions
        WHERE timestamp >= strftime('%s', date('now','start of month')) * 1000
        AND type = 'INCOME'
    """)
    fun getIncomeForCurrentMonth(): Flow<Int>

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions
        WHERE timestamp >= strftime('%s', date('now','start of month')) * 1000
        AND type = 'EXPENSE'
    """)
    fun getExpensesForCurrentMonth(): Flow<Int>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'INCOME'")
    fun getAllIncomeTotal(): Flow<Int>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'EXPENSE'")
    fun getAllExpensesTotal(): Flow<Int>

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions
        WHERE timestamp >= :from AND type = 'EXPENSE'
    """)
    fun getExpensesFrom(from: Long): Flow<Int>

    @Query("""
        SELECT * FROM transactions
        WHERE timestamp >= :from AND timestamp <= :to
        ORDER BY timestamp DESC
    """)
    fun getTransactionsInRange(from: Long, to: Long): Flow<List<Transaction>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'EXPENSE' AND timestamp >= :dayStart AND timestamp < :dayEnd")
    suspend fun getDailyExpense(dayStart: Long, dayEnd: Long): Int

    @Query("SELECT * FROM transactions WHERE timestamp >= :timestamp ORDER BY timestamp DESC")
    suspend fun getTransactionsSince(timestamp: Long): List<Transaction>

    @Query("SELECT * FROM transactions WHERE timestamp >= :timestamp AND type = 'EXPENSE'")
    suspend fun getExpensesSince(timestamp: Long): List<Transaction>

    @Query("SELECT * FROM transactions WHERE timestamp >= :timestamp AND type = 'INCOME'")
    suspend fun getIncomeSince(timestamp: Long): List<Transaction>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'EXPENSE' AND timestamp >= :from AND timestamp < :to")
    fun getExpensesInRange(from: Long, to: Long): Flow<Int>
}
