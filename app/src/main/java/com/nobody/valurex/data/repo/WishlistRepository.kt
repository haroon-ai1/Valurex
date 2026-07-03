package com.nobody.valurex.data.repo

import android.content.Context
import com.nobody.valurex.data.db.dao.WishlistDao
import com.nobody.valurex.data.db.entity.WishlistItem
import com.nobody.valurex.widget.WidgetUpdater
import kotlinx.coroutines.flow.Flow

class WishlistRepository(
    private val dao: WishlistDao,
    private val context: Context
) {
    fun getAll(): Flow<List<WishlistItem>>  = dao.getAll()
    fun getCount(): Flow<Int>               = dao.getCount()
    fun getTotal(): Flow<Int>               = dao.getTotal()

    suspend fun insert(item: WishlistItem): Long {
        val id = dao.insert(item)
        WidgetUpdater.updateWishlistWidget(context)
        return id
    }

    suspend fun update(item: WishlistItem) {
        dao.update(item)
        WidgetUpdater.updateWishlistWidget(context)
    }

    suspend fun delete(item: WishlistItem) {
        dao.delete(item)
        WidgetUpdater.updateWishlistWidget(context)
    }

    suspend fun getNthItem(offset: Int): WishlistItem? = dao.getNthItem(offset)
}
