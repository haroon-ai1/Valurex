package com.nobody.valurex.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wishlist_items")
data class WishlistItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val price: Int? = null,
    val note: String? = null,
    val imageUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
