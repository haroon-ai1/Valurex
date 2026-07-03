package com.nobody.valurex.data.repo

import android.content.Context
import com.nobody.valurex.data.db.dao.BudgetSettingsDao
import com.nobody.valurex.data.db.entity.BudgetSettings
import com.nobody.valurex.widget.WidgetUpdater
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BudgetRepository(
    private val dao: BudgetSettingsDao,
    private val context: Context
) {
    fun getBudget(): Flow<Int> = dao.getBudget()

    fun getBudgetSettings(): Flow<BudgetSettings> =
        dao.getBudgetSettings().map { it ?: BudgetSettings() }

    suspend fun saveSettings(settings: BudgetSettings) {
        dao.upsert(settings)
        WidgetUpdater.updateMoneyWidget(context)
    }
}
