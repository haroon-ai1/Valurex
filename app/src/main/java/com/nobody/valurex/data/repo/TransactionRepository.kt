package com.nobody.valurex.data.repo

import android.content.Context
import com.nobody.valurex.data.db.dao.CategoryDao
import com.nobody.valurex.data.db.dao.KeywordMapDao
import com.nobody.valurex.data.db.dao.TransactionDao
import com.nobody.valurex.data.db.entity.Transaction
import com.nobody.valurex.parser.ParserResult
import com.nobody.valurex.parser.TransactionParser
import com.nobody.valurex.parser.TransactionType
import com.nobody.valurex.widget.WidgetUpdater
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields

sealed class ParseResult {
    data class Success(val transaction: Transaction) : ParseResult()
    data class NeedsCategory(val amount: Int, val unknownText: String) : ParseResult()
    object ParseError : ParseResult()
}

class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val keywordMapDao: KeywordMapDao,
    private val categoryDao: CategoryDao,
    private val parser: TransactionParser,
    private val context: Context
) {
    fun getAll(): Flow<List<Transaction>> = transactionDao.getAll()
    fun getForCurrentMonth(): Flow<List<Transaction>> = transactionDao.getForCurrentMonth()
    fun getByCategory(id: Long): Flow<List<Transaction>> = transactionDao.getByCategory(id)
    fun getIncomeForCurrentMonth(): Flow<Int>  = transactionDao.getIncomeForCurrentMonth()
    fun getExpensesForCurrentMonth(): Flow<Int> = transactionDao.getExpensesForCurrentMonth()
    fun getAllIncomeTotal(): Flow<Int>            = transactionDao.getAllIncomeTotal()
    fun getAllExpensesTotal(): Flow<Int>          = transactionDao.getAllExpensesTotal()
    fun getExpensesFrom(from: Long): Flow<Int>   = transactionDao.getExpensesFrom(from)

    fun getWeekStart(date: LocalDate = LocalDate.now()): LocalDateTime {
        val weekFields = WeekFields.of(DayOfWeek.MONDAY, 1)
        val monday = date.with(weekFields.dayOfWeek(), 1)
        return monday.atStartOfDay()
    }

    fun getThisWeekSpendingFlow(): Flow<Int> {
        val zone = ZoneId.systemDefault()
        val monday = LocalDate.now(zone).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val from = monday.atStartOfDay(zone).toInstant().toEpochMilli()
        return transactionDao.getExpensesFrom(from)
    }

    fun getLastWeekSpendingFlow(): Flow<Int> {
        val zone = ZoneId.systemDefault()
        val thisMonday = LocalDate.now(zone).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val lastMonday = thisMonday.minusWeeks(1)
        val from = lastMonday.atStartOfDay(zone).toInstant().toEpochMilli()
        val to   = thisMonday.atStartOfDay(zone).toInstant().toEpochMilli()
        return transactionDao.getExpensesInRange(from, to)
    }

    suspend fun getThisWeekSpending(): Int = getThisWeekSpendingFlow().first()
    suspend fun getLastWeekSpending(): Int = getLastWeekSpendingFlow().first()

    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.update(transaction)
        WidgetUpdater.updateMoneyWidget(context)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.delete(transaction)
        WidgetUpdater.updateMoneyWidget(context)
    }

    suspend fun insertTransaction(
        amount: Int, categoryId: Long, note: String?, source: String,
        type: TransactionType = TransactionType.EXPENSE
    ): Transaction {
        val t = Transaction(
            amount = amount, categoryId = categoryId,
            note = note, timestamp = System.currentTimeMillis(),
            source = source, type = type.name
        )
        val id = transactionDao.insert(t)
        WidgetUpdater.updateMoneyWidget(context)
        return t.copy(id = id)
    }

    suspend fun parseAndInsert(
        text: String,
        type: TransactionType = TransactionType.EXPENSE
    ): ParseResult {
        if (type == TransactionType.INCOME) {
            val amount = TransactionParser.extractAmount(text) ?: return ParseResult.ParseError
            val incomeCategory = categoryDao.getByName("Income") ?: return ParseResult.ParseError
            val note = TransactionParser.extractRemaining(text).ifBlank { null }
            return ParseResult.Success(
                insertTransaction(amount, incomeCategory.id, note, "auto", TransactionType.INCOME)
            )
        }
        return when (val r = parser.parse(text)) {
            is ParserResult.Matched -> {
                val note = TransactionParser.extractRemaining(text).ifBlank { null }
                ParseResult.Success(insertTransaction(r.amount, r.categoryId, note, "auto"))
            }
            is ParserResult.Unmatched -> ParseResult.NeedsCategory(r.amount, r.unknownText)
            ParserResult.NoAmount    -> ParseResult.ParseError
        }
    }
}
