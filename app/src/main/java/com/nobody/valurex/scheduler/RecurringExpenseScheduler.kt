package com.nobody.valurex.scheduler

import com.nobody.valurex.data.db.ValurexDatabase
import com.nobody.valurex.data.db.entity.Transaction
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

object RecurringExpenseScheduler {

    suspend fun checkAndGenerate(db: ValurexDatabase) {
        val now  = System.currentTimeMillis()
        val due  = db.recurringExpenseDao().getDue(now)
        for (expense in due) {
            db.transactionDao().insert(
                Transaction(
                    amount     = expense.amount,
                    categoryId = expense.categoryId,
                    note       = expense.name,
                    timestamp  = now,
                    source     = "recurring",
                    type       = "EXPENSE"
                )
            )
            val next = calculateNextRun(expense.frequency, expense.dayOfPeriod, now)
            db.recurringExpenseDao().update(expense.copy(nextRun = next))
        }
    }

    private fun calculateNextRun(frequency: String, dayOfPeriod: Int, from: Long): Long {
        val zone = ZoneId.systemDefault()
        val date = Instant.ofEpochMilli(from).atZone(zone).toLocalDate()
        val next = when (frequency) {
            "WEEKLY" -> {
                val target = DayOfWeek.of(dayOfPeriod)
                date.plusWeeks(1).with(TemporalAdjusters.nextOrSame(target))
            }
            else -> {
                val nextMonth = date.plusMonths(1)
                nextMonth.withDayOfMonth(minOf(dayOfPeriod, nextMonth.lengthOfMonth()))
            }
        }
        return next.atStartOfDay(zone).toInstant().toEpochMilli()
    }
}
