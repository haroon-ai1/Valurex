package com.nobody.valurex.data.db.dao

import androidx.room.*
import com.nobody.valurex.data.db.entity.BudgetSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetSettingsDao {
    @Query("SELECT monthlyBudget FROM budget_settings WHERE id = 1")
    fun getBudget(): Flow<Int>

    @Query("SELECT * FROM budget_settings WHERE id = 1")
    fun getBudgetSettings(): Flow<BudgetSettings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(settings: BudgetSettings)
}
