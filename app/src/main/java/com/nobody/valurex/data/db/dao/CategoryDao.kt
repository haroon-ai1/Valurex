package com.nobody.valurex.data.db.dao

import androidx.room.*
import com.nobody.valurex.data.db.entity.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(category: Category): Long

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)

    @Query("SELECT * FROM Category ORDER BY name")
    fun getAll(): Flow<List<Category>>

    @Query("SELECT * FROM Category WHERE id = :id")
    suspend fun getById(id: Long): Category?

    @Query("SELECT * FROM Category WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): Category?
}
