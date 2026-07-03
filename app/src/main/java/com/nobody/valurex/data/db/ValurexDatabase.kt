package com.nobody.valurex.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.nobody.valurex.data.db.dao.*
import com.nobody.valurex.data.db.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Category::class,
        Transaction::class,
        RecurringExpense::class,
        KeywordMap::class,
        WalletLog::class,
        BudgetSettings::class,
        Loan::class,
        com.nobody.valurex.data.db.entity.WishlistItem::class,
        ReminderSettings::class
    ],
    version = 5,
    exportSchema = false
)
abstract class ValurexDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun recurringExpenseDao(): RecurringExpenseDao
    abstract fun keywordMapDao(): KeywordMapDao
    abstract fun walletLogDao(): WalletLogDao
    abstract fun budgetSettingsDao(): BudgetSettingsDao
    abstract fun loanDao(): LoanDao
    abstract fun wishlistDao(): com.nobody.valurex.data.db.dao.WishlistDao
    abstract fun reminderSettingsDao(): ReminderSettingsDao

    companion object {
        @Volatile private var INSTANCE: ValurexDatabase? = null

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS reminder_settings (
                        id INTEGER PRIMARY KEY NOT NULL,
                        nightly_enabled INTEGER NOT NULL DEFAULT 0,
                        nightly_hour INTEGER NOT NULL DEFAULT 22,
                        nightly_minute INTEGER NOT NULL DEFAULT 0,
                        daily1_enabled INTEGER NOT NULL DEFAULT 0,
                        daily1_hour INTEGER NOT NULL DEFAULT 12,
                        daily1_minute INTEGER NOT NULL DEFAULT 0,
                        daily2_enabled INTEGER NOT NULL DEFAULT 0,
                        daily2_hour INTEGER NOT NULL DEFAULT 18,
                        daily2_minute INTEGER NOT NULL DEFAULT 0,
                        daily3_enabled INTEGER NOT NULL DEFAULT 0,
                        daily3_hour INTEGER NOT NULL DEFAULT 21,
                        daily3_minute INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
                db.execSQL("INSERT OR IGNORE INTO reminder_settings (id) VALUES (1)")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS wishlist_items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        price INTEGER,
                        note TEXT,
                        imageUri TEXT,
                        createdAt INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE budget_settings ADD COLUMN budgetPeriod TEXT NOT NULL DEFAULT 'MONTHLY'"
                )
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN type TEXT NOT NULL DEFAULT 'EXPENSE'")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS budget_settings (
                        id INTEGER NOT NULL PRIMARY KEY,
                        monthlyBudget INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
                db.execSQL(
                    "INSERT OR IGNORE INTO budget_settings (id, monthlyBudget) VALUES (1, 0)"
                )

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS loans (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        personName TEXT NOT NULL,
                        amount INTEGER NOT NULL,
                        direction TEXT NOT NULL,
                        note TEXT,
                        createdAt INTEGER NOT NULL,
                        settledAt INTEGER
                    )
                """.trimIndent())

                db.execSQL("""
                    INSERT INTO Category (name, color, monthlyLimit, isDefault)
                    SELECT 'Income', '#4CAF50', NULL, 1
                    WHERE NOT EXISTS (SELECT 1 FROM Category WHERE name = 'Income')
                """.trimIndent())
            }
        }

        fun getInstance(context: Context): ValurexDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ValurexDatabase::class.java,
                    "valurex.db"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .addCallback(SeedCallback)
                .build()
                .also { INSTANCE = it }
            }

        private val SeedCallback = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch { seed(database) }
                }
            }
        }

        private suspend fun seed(db: ValurexDatabase) {
            val categoryDao = db.categoryDao()
            val keywordDao  = db.keywordMapDao()

            val foodId  = categoryDao.insert(Category(name = "Food",      color = "#FFA726", monthlyLimit = null, isDefault = true))
            val transId = categoryDao.insert(Category(name = "Transport", color = "#42A5F5", monthlyLimit = null, isDefault = true))
            val billsId = categoryDao.insert(Category(name = "Bills",     color = "#EF5350", monthlyLimit = null, isDefault = true))
            val miscId  = categoryDao.insert(Category(name = "Misc",      color = "#9E9E9E", monthlyLimit = null, isDefault = true))
            categoryDao.insert(Category(name = "Income", color = "#4CAF50", monthlyLimit = null, isDefault = true))

            db.budgetSettingsDao().upsert(BudgetSettings(id = 1, monthlyBudget = 0))

            val keywords = listOf(
                "biryani" to foodId, "karahi" to foodId, "chai" to foodId,
                "pizza" to foodId, "burger" to foodId, "samosa" to foodId,
                "paratha" to foodId, "naan" to foodId, "dahi bhalla" to foodId,
                "golgappa" to foodId, "chicken" to foodId, "grocery" to foodId,
                "ration" to foodId, "shawarma" to foodId, "bbq" to foodId,
                "uber" to transId, "careem" to transId, "indrive" to transId,
                "bykea" to transId, "fuel" to transId, "petrol" to transId,
                "diesel" to transId, "rickshaw" to transId, "bus" to transId,
                "metro" to transId,
                "rent" to billsId, "electricity" to billsId, "gas" to billsId,
                "internet" to billsId, "wifi" to billsId, "mobile" to billsId,
                "recharge" to billsId,
                "misc" to miscId, "other" to miscId
            )
            keywords.forEach { (kw, catId) ->
                keywordDao.insert(KeywordMap(keyword = kw.lowercase(), categoryId = catId))
            }
        }
    }
}
