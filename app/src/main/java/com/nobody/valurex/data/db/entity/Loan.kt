package com.nobody.valurex.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "loans")
data class Loan(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personName: String,
    val amount: Int,
    val direction: String,
    val note: String?,
    val createdAt: Long,
    val settledAt: Long? = null
)
