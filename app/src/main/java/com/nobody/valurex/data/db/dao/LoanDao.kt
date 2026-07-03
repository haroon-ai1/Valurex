package com.nobody.valurex.data.db.dao

import androidx.room.*
import com.nobody.valurex.data.db.entity.Loan
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(loan: Loan): Long

    @Update
    suspend fun update(loan: Loan)

    @Delete
    suspend fun delete(loan: Loan)

    @Query("""
        SELECT * FROM loans
        ORDER BY CASE WHEN settledAt IS NULL THEN 0 ELSE 1 END, createdAt DESC
    """)
    fun getAll(): Flow<List<Loan>>

    @Query("UPDATE loans SET settledAt = :timestamp WHERE id = :id")
    suspend fun markSettled(id: Long, timestamp: Long)

    @Query("DELETE FROM loans WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM loans WHERE LOWER(personName) = LOWER(:name) AND direction = :direction AND settledAt IS NULL LIMIT 1")
    suspend fun findActiveByName(name: String, direction: String): Loan?
}
