package com.nobody.valurex

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.nobody.valurex.data.db.ValurexDatabase
import com.nobody.valurex.data.repo.BudgetRepository
import com.nobody.valurex.data.repo.CategoryRepository
import com.nobody.valurex.data.repo.LoanRepository
import com.nobody.valurex.data.repo.ReminderRepository
import com.nobody.valurex.data.repo.SavingsBarPrefs
import com.nobody.valurex.data.repo.TransactionRepository
import com.nobody.valurex.data.repo.WishlistRepository
import com.nobody.valurex.parser.TransactionParser
import com.nobody.valurex.scheduler.RecurringExpenseScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ValurexApplication : Application() {
    val database by lazy { ValurexDatabase.getInstance(this) }
    val transactionRepository by lazy {
        TransactionRepository(
            transactionDao = database.transactionDao(),
            keywordMapDao  = database.keywordMapDao(),
            categoryDao    = database.categoryDao(),
            parser         = TransactionParser(database.keywordMapDao()),
            context        = this
        )
    }
    val categoryRepository  by lazy { CategoryRepository(database.categoryDao()) }
    val budgetRepository    by lazy { BudgetRepository(database.budgetSettingsDao(), context = this) }
    val loanRepository      by lazy { LoanRepository(database.loanDao()) }
    val wishlistRepository  by lazy { WishlistRepository(database.wishlistDao(), context = this) }
    val reminderRepository  by lazy { ReminderRepository(database.reminderSettingsDao(), this) }
    val savingsBarPrefs     by lazy { SavingsBarPrefs(this) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        CoroutineScope(Dispatchers.IO).launch {
            RecurringExpenseScheduler.checkAndGenerate(database)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Valurex Reminders"
            val descriptionText = "Daily nudges and nightly wallet check-in"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("valurex_reminders", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
