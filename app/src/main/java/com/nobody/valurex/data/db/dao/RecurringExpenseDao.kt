package com.nobody.valurex.data.db.dao

import androidx.room.*
import com.nobody.valurex.data.db.entity.RecurringExpense
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringExpenseDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(expense: RecurringExpense): Long

    @Update
    suspend fun update(expense: RecurringExpense)

    @Delete
    suspend fun delete(expense: RecurringExpense)

    @Query("SELECT * FROM RecurringExpense ORDER BY name")
    fun getAll(): Flow<List<RecurringExpense>>

    @Query("SELECT * FROM RecurringExpense WHERE nextRun <= :now")
    suspend fun getDue(now: Long): List<RecurringExpense>
}
