package com.nobody.valurex.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nobody.valurex.ValurexApplication
import com.nobody.valurex.data.db.entity.Category
import com.nobody.valurex.data.db.entity.Transaction
import com.nobody.valurex.ui.util.currentMonthStartMillis
import com.nobody.valurex.ui.util.currentWeekStartMillis
import com.nobody.valurex.ui.util.last30DaysStartMillis
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class TimeRange(val label: String) {
    THIS_WEEK("This week"),
    THIS_MONTH("This month"),
    LAST_30_DAYS("30 days"),
    ALL_TIME("All time")
}

data class CategoryStat(
    val category: Category,
    val amount: Int,
    val count: Int,
    val percent: Float
)

data class DailyStat(val label: String, val amount: Int)

data class StatsUiState(
    val timeRange: TimeRange = TimeRange.THIS_MONTH,
    val totalSpent: Int = 0,
    val totalIncome: Int = 0,
    val net: Int = 0,
    val avgDailySpend: Int = 0,
    val categoryStats: List<CategoryStat> = emptyList(),
    val dailyStats: List<DailyStat> = emptyList(),
    val biggestTransaction: TransactionWithCategory? = null
)

class StatsViewModel(application: Application) : AndroidViewModel(application) {
    private val app            = application as ValurexApplication
    private val transactionDao = app.database.transactionDao()
    private val categoryRepo   = app.categoryRepository

    private val _range = MutableStateFlow(TimeRange.THIS_MONTH)
    val selectedRange: StateFlow<TimeRange> = _range.asStateFlow()

    val uiState: StateFlow<StatsUiState> =
        combine(_range, categoryRepo.getAll()) { r, cats -> r to cats }
            .flatMapLatest { (range, cats) ->
                val (from, _) = range.dateRange()
                transactionDao.getTransactionsInRange(from, Long.MAX_VALUE).map { txns ->
                    buildState(range, txns, cats)
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsUiState())

    fun selectRange(range: TimeRange) { _range.value = range }

    private fun buildState(
        range: TimeRange,
        txns: List<Transaction>,
        cats: List<Category>
    ): StatsUiState {
        val catMap   = cats.associateBy { it.id }
        val expenses = txns.filter { it.type == "EXPENSE" }
        val incomes  = txns.filter { it.type == "INCOME" }

        val totalSpent  = expenses.sumOf { it.amount }
        val totalIncome = incomes.sumOf { it.amount }
        val net         = totalIncome - totalSpent

        val (from, _)     = range.dateRange()
        val elapsedDays   = maxOf(1, ((System.currentTimeMillis() - from) / 86_400_000L).toInt())
        val avgDailySpend = totalSpent / elapsedDays

        val catStats = expenses.groupBy { it.categoryId }
            .mapNotNull { (catId, list) ->
                val cat    = catMap[catId] ?: return@mapNotNull null
                val amount = list.sumOf { it.amount }
                CategoryStat(
                    category = cat,
                    amount   = amount,
                    count    = list.size,
                    percent  = if (totalSpent > 0) amount.toFloat() / totalSpent else 0f
                )
            }
            .sortedByDescending { it.amount }

        val zone       = ZoneId.systemDefault()
        val dayFmt     = DateTimeFormatter.ofPattern("MMM d")
        val dailyStats = expenses
            .groupBy { Instant.ofEpochMilli(it.timestamp).atZone(zone).toLocalDate() }
            .entries.sortedBy { it.key }
            .map { (date, list) ->
                DailyStat(label = date.format(dayFmt), amount = list.sumOf { it.amount })
            }

        val biggest = expenses.maxByOrNull { it.amount }?.let { t ->
            catMap[t.categoryId]?.let { cat -> TransactionWithCategory(t, cat) }
        }

        return StatsUiState(
            timeRange          = range,
            totalSpent         = totalSpent,
            totalIncome        = totalIncome,
            net                = net,
            avgDailySpend      = avgDailySpend,
            categoryStats      = catStats,
            dailyStats         = dailyStats,
            biggestTransaction = biggest
        )
    }
}

private fun TimeRange.dateRange(): Pair<Long, Long> {
    val from = when (this) {
        TimeRange.THIS_WEEK    -> currentWeekStartMillis()
        TimeRange.THIS_MONTH   -> currentMonthStartMillis()
        TimeRange.LAST_30_DAYS -> last30DaysStartMillis()
        TimeRange.ALL_TIME     -> 0L
    }
    return Pair(from, Long.MAX_VALUE)
}
