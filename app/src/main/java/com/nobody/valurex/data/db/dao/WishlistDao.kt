package com.nobody.valurex.data.db.dao

import androidx.room.*
import com.nobody.valurex.data.db.entity.WishlistItem
import kotlinx.coroutines.flow.Flow

@Dao
interface WishlistDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(item: WishlistItem): Long

    @Update
    suspend fun update(item: WishlistItem)

    @Delete
    suspend fun delete(item: WishlistItem)

    @Query("SELECT * FROM wishlist_items ORDER BY createdAt DESC")
    fun getAll(): Flow<List<WishlistItem>>

    @Query("SELECT COUNT(*) FROM wishlist_items")
    fun getCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(price), 0) FROM wishlist_items")
    fun getTotal(): Flow<Int>

    @Query("SELECT * FROM wishlist_items ORDER BY createdAt DESC LIMIT 1 OFFSET :offset")
    suspend fun getNthItem(offset: Int): WishlistItem?
}
