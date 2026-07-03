package com.nobody.valurex.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class WalletLog(
    @PrimaryKey val date: String,
    val reportedBalance: Int
)
