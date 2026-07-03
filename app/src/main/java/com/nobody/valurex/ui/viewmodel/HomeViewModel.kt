package com.nobody.valurex.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nobody.valurex.ValurexApplication
import com.nobody.valurex.data.db.entity.Category
import com.nobody.valurex.data.db.entity.KeywordMap
import com.nobody.valurex.data.db.entity.Transaction
import com.nobody.valurex.data.repo.ParseResult
import com.nobody.valurex.parser.TransactionParser
import com.nobody.valurex.ui.util.currentWeekStartMillis
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class TransactionWithCategory(
    val transaction: Transaction,
    val category: Category
)

data class HomeUiState(
    val transactions: List<TransactionWithCategory> = emptyList(),
    val categories: List<Category> = emptyList(),
    val monthlyTotal: Int = 0,
    val categoryTotals: Map<Long, Int> = emptyMap(),
    val pendingNeedsCategory: ParseResult.NeedsCategory? = null,
    val monthlyBudget: Int = 0,
    val totalMoney: Int = 0,
    val monthlyExpenses: Int = 0,
    val budgetRemaining: Int = 0,
    val isOverBudget: Boolean = false,
    val budgetPeriod: String = "MONTHLY",
    val savingsBarVisible: Boolean = false,
    val savingsBarSavedAmount: Int = 0,
    val savingsBarPercentLess: Int = 0
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val app             = application as ValurexApplication
    private val transactionRepo = app.transactionRepository
    private val categoryRepo    = app.categoryRepository
    private val budgetRepo      = app.budgetRepository
    private val savingsBarPrefs = app.savingsBarPrefs

    private val _pendingNeedsCategory = MutableStateFlow<ParseResult.NeedsCategory?>(null)

    private val _dbStream: Flow<HomeUiState> = combine(
        combine(
            transactionRepo.getForCurrentMonth(),
            categoryRepo.getAll()
        ) { txns, cats -> txns to cats },
        budgetRepo.getBudgetSettings().flatMapLatest { settings ->
            val expenseFlow = if (settings.budgetPeriod == "WEEKLY")
                transactionRepo.getExpensesFrom(currentWeekStartMillis())
            else
                transactionRepo.getExpensesForCurrentMonth()
            expenseFlow.map { exp -> Triple(exp, settings.monthlyBudget, settings.budgetPeriod) }
        },
        combine(
            transactionRepo.getAllIncomeTotal(),
            transactionRepo.getAllExpensesTotal()
        ) { allInc, allExp -> allInc - allExp }
    ) { (txns, cats), (periodExp, budget, period), totalMoney ->
        val catMap          = cats.associateBy { it.id }
        val budgetRemaining = budget - periodExp
        HomeUiState(
            transactions    = txns.mapNotNull { t ->
                catMap[t.categoryId]?.let { TransactionWithCategory(t, it) }
            },
            categories      = cats,
            monthlyTotal    = periodExp,
            categoryTotals  = txns.filter { it.type == "EXPENSE" }
                .groupBy { it.categoryId }
                .mapValues { (_, ts) -> ts.sumOf { it.amount } },
            monthlyBudget   = budget,
            totalMoney      = totalMoney,
            monthlyExpenses = periodExp,
            budgetRemaining = budgetRemaining,
            isOverBudget    = budgetRemaining < 0,
            budgetPeriod    = period
        )
    }

    val uiState: StateFlow<HomeUiState> = combine(
        _dbStream,
        _pendingNeedsCategory,
        transactionRepo.getThisWeekSpendingFlow(),
        transactionRepo.getLastWeekSpendingFlow(),
        savingsBarPrefs.dismissedWeekFlow
    ) { state, pending, thisWeek, lastWeek, dismissedWeek ->
        val saved        = lastWeek - thisWeek
        val percentLess  = if (lastWeek > 0) (saved * 100 / lastWeek) else 0
        val thresholdMet = saved >= 500 || percentLess >= 10
        val weekStart    = currentWeekStartMillis()
        val visible      = saved > 0 && thresholdMet && dismissedWeek != weekStart && lastWeek > 0
        state.copy(
            pendingNeedsCategory = pending,
            savingsBarVisible    = visible,
            savingsBarSavedAmount = saved.coerceAtLeast(0),
            savingsBarPercentLess = percentLess.coerceIn(0, 100)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun onAddTransaction(text: String) {
        viewModelScope.launch {
            try {
                val type = TransactionParser.detectType(text)
                when (val r = transactionRepo.parseAndInsert(text, type)) {
                    is ParseResult.NeedsCategory -> _pendingNeedsCategory.value = r
                    else -> Unit
                }
            } catch (_: Exception) { /* parse failures are silent */ }
        }
    }

    fun onResolveCategory(amount: Int, text: String, categoryId: Long, learnKeyword: Boolean) {
        viewModelScope.launch {
            try {
                transactionRepo.insertTransaction(amount, categoryId, text.trim().ifBlank { null }, "manual")
                if (learnKeyword && text.isNotBlank()) {
                    val kw = text.trim().lowercase().split(Regex("\\s+")).first()
                    app.database.keywordMapDao().insert(KeywordMap(keyword = kw, categoryId = categoryId))
                }
            } catch (_: Exception) { /* silently ignore */ }
            _pendingNeedsCategory.value = null
        }
    }

    fun dismissNeedsCategory() { _pendingNeedsCategory.value = null }

    fun dismissSavingsBar() {
        viewModelScope.launch {
            savingsBarPrefs.dismissForWeek(currentWeekStartMillis())
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try { transactionRepo.updateTransaction(transaction) } catch (_: Exception) {}
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try { transactionRepo.deleteTransaction(transaction) } catch (_: Exception) {}
        }
    }
}
