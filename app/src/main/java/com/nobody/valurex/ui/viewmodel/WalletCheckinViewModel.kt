package com.nobody.valurex.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nobody.valurex.ValurexApplication
import com.nobody.valurex.data.db.entity.Category
import com.nobody.valurex.data.db.entity.Transaction
import com.nobody.valurex.data.db.entity.WalletLog
import com.nobody.valurex.parser.TransactionType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

data class WalletCheckinState(
    val expectedAmount: Int = 0,
    val todayTransactions: List<TransactionWithCategory> = emptyList()
)

class WalletCheckinViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as ValurexApplication
    private val transRepo = app.transactionRepository
    private val db = app.database
    private val walletLogDao = db.walletLogDao()
    private val transactionDao = db.transactionDao()
    private val categoryDao = db.categoryDao()

    private val _uiState = MutableStateFlow(WalletCheckinState())
    val uiState: StateFlow<WalletCheckinState> = _uiState.asStateFlow()

    init {
        loadState()
    }

    private fun loadState() {
        viewModelScope.launch {
            val latestLog = walletLogDao.getLatest()
            val startTime = if (latestLog != null) {
                // date is in "yyyy-MM-dd"
                LocalDate.parse(latestLog.date).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            } else 0L

            val income = transactionDao.getIncomeSince(startTime).sumOf { it.amount }
            val expenses = transactionDao.getExpensesSince(startTime).sumOf { it.amount }
            
            val base = latestLog?.reportedBalance ?: (transRepo.getAllIncomeTotal().first() - transRepo.getAllExpensesTotal().first())
            val expected = base + income - expenses

            val todayStart = LocalDate.now().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            val todayTrans = transactionDao.getTransactionsSince(todayStart).map { t ->
                val cat = categoryDao.getById(t.categoryId) ?: Category(name = "Misc", color = "#9E9E9E", monthlyLimit = null, isDefault = false)
                TransactionWithCategory(t, cat)
            }

            _uiState.value = WalletCheckinState(expected, todayTrans)
        }
    }

    fun reconcile(actual: Int, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            val expected = _uiState.value.expectedAmount
            val diff = expected - actual
            
            if (diff == 0) {
                saveLog(actual)
                onComplete("Wallet matches! Great tracking 🎯")
            } else if (diff > 0) {
                // Unaccounted spending
                val misc = categoryDao.getByName("Misc")
                transRepo.insertTransaction(
                    amount = diff,
                    categoryId = misc?.id ?: 1L,
                    note = "Wallet reconciliation untracked",
                    source = "RECONCILE",
                    type = TransactionType.EXPENSE
                )
                saveLog(actual)
                onComplete("Reconciled Rs $diff to Misc")
            } else {
                // Unaccounted income
                val incomeCat = categoryDao.getByName("Income")
                transRepo.insertTransaction(
                    amount = -diff,
                    categoryId = incomeCat?.id ?: 1L,
                    note = "Wallet reconciliation untracked",
                    source = "RECONCILE",
                    type = TransactionType.INCOME
                )
                saveLog(actual)
                onComplete("Reconciled Rs ${-diff} to Income")
            }
        }
    }

    private suspend fun saveLog(balance: Int) {
        walletLogDao.insertOrUpdate(WalletLog(date = LocalDate.now().toString(), reportedBalance = balance))
    }
}
