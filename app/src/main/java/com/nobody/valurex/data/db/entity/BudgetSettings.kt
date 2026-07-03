package com.nobody.valurex.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budget_settings")
data class BudgetSettings(
    @PrimaryKey val id: Int = 1,
    val monthlyBudget: Int = 0,
    val budgetPeriod: String = "MONTHLY"
)
