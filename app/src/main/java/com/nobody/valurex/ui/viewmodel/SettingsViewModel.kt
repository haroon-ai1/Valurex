package com.nobody.valurex.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nobody.valurex.ValurexApplication
import com.nobody.valurex.data.db.DemoDataSeeder
import com.nobody.valurex.data.db.entity.BudgetSettings
import com.nobody.valurex.data.db.entity.Category
import com.nobody.valurex.data.db.entity.RecurringExpense
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val app          = application as ValurexApplication
    private val budgetRepo   = app.budgetRepository
    private val recurringDao = app.database.recurringExpenseDao()
    private val categoryRepo = app.categoryRepository

    val budgetSettings: StateFlow<BudgetSettings> = budgetRepo.getBudgetSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BudgetSettings())

    val recurringExpenses: StateFlow<List<RecurringExpense>> = recurringDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val categories: StateFlow<List<Category>> = categoryRepo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun saveSettings(settings: BudgetSettings) {
        viewModelScope.launch { budgetRepo.saveSettings(settings) }
    }

    fun addRecurring(name: String, amount: Int, categoryId: Long, frequency: String, dayOfPeriod: Int) {
        viewModelScope.launch {
            recurringDao.insert(
                RecurringExpense(
                    name        = name,
                    amount      = amount,
                    categoryId  = categoryId,
                    frequency   = frequency,
                    dayOfPeriod = dayOfPeriod,
                    nextRun     = firstNextRun(frequency, dayOfPeriod)
                )
            )
        }
    }

    fun updateRecurring(expense: RecurringExpense) {
        viewModelScope.launch { recurringDao.update(expense) }
    }

    fun deleteRecurring(expense: RecurringExpense) {
        viewModelScope.launch { recurringDao.delete(expense) }
    }

    fun seedDemoData(onComplete: () -> Unit) {
        viewModelScope.launch {
            DemoDataSeeder.seed(app.database)
            onComplete()
        }
    }

    private fun firstNextRun(frequency: String, dayOfPeriod: Int): Long {
        val zone  = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val next  = when (frequency) {
            "WEEKLY" -> {
                val target    = DayOfWeek.of(dayOfPeriod)
                val candidate = today.with(TemporalAdjusters.nextOrSame(target))
                if (!candidate.isAfter(today)) candidate.plusWeeks(1) else candidate
            }
            else -> {
                val maxDay    = today.lengthOfMonth()
                val candidate = today.withDayOfMonth(minOf(dayOfPeriod, maxDay))
                if (!candidate.isAfter(today)) {
                    val nm = today.plusMonths(1)
                    nm.withDayOfMonth(minOf(dayOfPeriod, nm.lengthOfMonth()))
                } else candidate
            }
        }
        return next.atStartOfDay(zone).toInstant().toEpochMilli()
    }
}
