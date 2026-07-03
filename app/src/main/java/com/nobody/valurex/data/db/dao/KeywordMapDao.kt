package com.nobody.valurex.data.db.dao

import androidx.room.*
import com.nobody.valurex.data.db.entity.Category
import com.nobody.valurex.data.db.entity.KeywordMap
import kotlinx.coroutines.flow.Flow

@Dao
interface KeywordMapDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(keywordMap: KeywordMap)

    @Query("""
        SELECT Category.* FROM Category
        INNER JOIN KeywordMap ON Category.id = KeywordMap.categoryId
        WHERE KeywordMap.keyword = :keyword
    """)
    suspend fun lookup(keyword: String): Category?

    @Query("SELECT * FROM KeywordMap ORDER BY keyword")
    fun getAll(): Flow<List<KeywordMap>>
}
