package com.nobody.valurex.data.db

import com.nobody.valurex.data.db.entity.*
import java.time.LocalDateTime
import java.time.ZoneId

object DemoDataSeeder {

    suspend fun seed(db: ValurexDatabase) {
        val catDao    = db.categoryDao()
        val txDao     = db.transactionDao()
        val loanDao   = db.loanDao()
        val wishDao   = db.wishlistDao()
        val recDao    = db.recurringExpenseDao()
        val budgetDao = db.budgetSettingsDao()
        val walletDao = db.walletLogDao()

        val zone = ZoneId.systemDefault()
        fun ts(y: Int, mo: Int, d: Int, h: Int = 10): Long =
            LocalDateTime.of(y, mo, d, h, 0).atZone(zone).toInstant().toEpochMilli()
        fun nextRun(mo: Int, d: Int): Long =
            LocalDateTime.of(2026, mo, d, 0, 0).atZone(zone).toInstant().toEpochMilli()

        // ── Categories ────────────────────────────────────────────────────────
        val foodId   = catDao.getByName("Food")?.id          ?: catDao.insert(Category(name = "Food",          color = "#FFA726", monthlyLimit = null, isDefault = false))
        val transId  = catDao.getByName("Transport")?.id     ?: catDao.insert(Category(name = "Transport",     color = "#42A5F5", monthlyLimit = null, isDefault = false))
        val billsId  = catDao.getByName("Bills")?.id         ?: catDao.insert(Category(name = "Bills",         color = "#EF5350", monthlyLimit = null, isDefault = false))
        val incomeId = catDao.getByName("Income")?.id        ?: catDao.insert(Category(name = "Income",        color = "#4CAF50", monthlyLimit = null, isDefault = false))
        val shopId   = catDao.getByName("Shopping")?.id      ?: catDao.insert(Category(name = "Shopping",      color = "#AB47BC", monthlyLimit = null, isDefault = false))
        val enterId  = catDao.getByName("Entertainment")?.id ?: catDao.insert(Category(name = "Entertainment", color = "#26C6DA", monthlyLimit = null, isDefault = false))
        val healthId = catDao.getByName("Health")?.id        ?: catDao.insert(Category(name = "Health",        color = "#EC407A", monthlyLimit = null, isDefault = false))

        // ── Budget ────────────────────────────────────────────────────────────
        budgetDao.upsert(BudgetSettings(id = 1, monthlyBudget = 80000, budgetPeriod = "MONTHLY"))

        // ── Transactions ─────────────────────────────────────────────────────
        data class T(val amt: Int, val cat: Long, val note: String, val y: Int, val mo: Int, val d: Int, val h: Int = 10, val type: String = "EXPENSE")

        val rows = listOf(
            // ── May income ──────────────────────────────────────────────────
            T(120000, incomeId, "Monthly salary",        2026, 5,  1,  9, "INCOME"),
            T( 15000, incomeId, "Freelance project",     2026, 5, 10, 14, "INCOME"),
            // ── May bills ───────────────────────────────────────────────────
            T( 35000, billsId,  "Rent",                  2026, 5,  2, 11),
            T(  9500, billsId,  "Electricity bill",      2026, 5,  5, 10),
            T(  2400, billsId,  "Gas bill",              2026, 5,  7, 10),
            T(  2000, billsId,  "Internet",              2026, 5,  8, 10),
            T(  1200, billsId,  "Mobile recharge",       2026, 5, 10, 10),
            T(  1500, enterId,  "Netflix",               2026, 5,  1, 12),
            T(  3000, healthId, "Gym membership",        2026, 5,  1,  8),
            // ── May food ────────────────────────────────────────────────────
            T(   650, foodId,   "Biryani lunch",         2026, 5,  3, 13),
            T(   120, foodId,   "Chai",                  2026, 5,  4,  9),
            T(  6800, foodId,   "Grocery ration",        2026, 5,  5, 17),
            T(  1800, foodId,   "Pizza dinner",          2026, 5,  8, 20),
            T(   950, foodId,   "Chicken karahi",        2026, 5, 11, 13),
            T(   180, foodId,   "Chai ×2",          2026, 5, 12, 10),
            T(  7200, foodId,   "Weekly grocery",        2026, 5, 12, 18),
            T(  1200, foodId,   "Burger + fries",        2026, 5, 15, 19),
            T(   550, foodId,   "Shawarma",              2026, 5, 17, 14),
            T(   100, foodId,   "Samosa + chai",         2026, 5, 19, 16),
            T(  6500, foodId,   "Grocery",               2026, 5, 19, 17),
            T(  2800, foodId,   "BBQ dinner",            2026, 5, 21, 21),
            T(   850, foodId,   "Chicken karahi",        2026, 5, 24, 13),
            T(  7400, foodId,   "Weekly grocery",        2026, 5, 26, 17),
            T(   450, foodId,   "Paratha roll",          2026, 5, 28,  8),
            T(  1600, foodId,   "Dinner out",            2026, 5, 30, 20),
            // ── May transport ───────────────────────────────────────────────
            T(   420, transId,  "Uber to office",        2026, 5,  4,  8),
            T(  5500, transId,  "Petrol",                2026, 5,  6, 11),
            T(   180, transId,  "Rickshaw",              2026, 5,  9,  9),
            T(   380, transId,  "Careem ride",           2026, 5, 13, 18),
            T(  5800, transId,  "Petrol fill",           2026, 5, 20, 11),
            T(   250, transId,  "Rickshaw",              2026, 5, 22,  9),
            T(   500, transId,  "Uber (late night)",     2026, 5, 25, 22),
            T(  6200, transId,  "Petrol",                2026, 5, 29, 11),
            // ── May shopping / health / entertainment ────────────────────────
            T(  6500, shopId,   "Summer clothes",        2026, 5, 14, 15),
            T(  8500, shopId,   "Nike sneakers",         2026, 5, 23, 14),
            T(  2200, enterId,  "Cinema tickets",        2026, 5, 15, 20),
            T(   850, healthId, "Medicine",              2026, 5, 22, 10),
            // ── June income ─────────────────────────────────────────────────
            T(120000, incomeId, "Monthly salary",        2026, 6,  1,  9, "INCOME"),
            T(  8000, incomeId, "Side project payment",  2026, 6, 15, 14, "INCOME"),
            // ── June bills ──────────────────────────────────────────────────
            T( 35000, billsId,  "Rent",                  2026, 6,  2, 11),
            T( 11200, billsId,  "Electricity bill",      2026, 6,  5, 10),
            T(  2800, billsId,  "Gas bill",              2026, 6,  7, 10),
            T(  2000, billsId,  "Internet",              2026, 6,  8, 10),
            T(  1200, billsId,  "Mobile recharge",       2026, 6, 10, 10),
            T(  1500, enterId,  "Netflix",               2026, 6,  1, 12),
            T(  3000, healthId, "Gym membership",        2026, 6,  1,  8),
            // ── June food ───────────────────────────────────────────────────
            T(   700, foodId,   "Biryani",               2026, 6,  3, 13),
            T(   130, foodId,   "Chai",                  2026, 6,  4,  9),
            T(  7100, foodId,   "Monthly grocery",       2026, 6,  4, 17),
            T(  1900, foodId,   "Pizza night",           2026, 6,  6, 20),
            T(  1100, foodId,   "Karahi with friends",   2026, 6,  9, 21),
            T(   160, foodId,   "Chai",                  2026, 6, 11, 10),
            T(  6800, foodId,   "Weekly grocery",        2026, 6, 11, 18),
            T(  1350, foodId,   "Burger meal",           2026, 6, 13, 19),
            T(   600, foodId,   "Shawarma wrap",         2026, 6, 16, 14),
            T(    90, foodId,   "Samosa",                2026, 6, 17, 16),
            T(  7300, foodId,   "Grocery",               2026, 6, 18, 17),
            T(  3200, foodId,   "BBQ with family",       2026, 6, 20, 21),
            T(   900, foodId,   "Chicken karahi",        2026, 6, 22, 13),
            T(  6900, foodId,   "Weekly grocery",        2026, 6, 25, 17),
            T(   480, foodId,   "Paratha + chai",        2026, 6, 27,  8),
            T(  1700, foodId,   "Dinner out",            2026, 6, 28, 20),
            // ── June transport ──────────────────────────────────────────────
            T(   450, transId,  "Uber to office",        2026, 6,  3,  8),
            T(  5900, transId,  "Petrol",                2026, 6,  5, 11),
            T(   200, transId,  "Rickshaw",              2026, 6,  9,  9),
            T(   420, transId,  "Careem",                2026, 6, 12, 18),
            T(  6100, transId,  "Petrol",                2026, 6, 19, 11),
            T(   220, transId,  "Rickshaw",              2026, 6, 21,  9),
            T(   480, transId,  "Uber",                  2026, 6, 24, 22),
            // ── June shopping / health / entertainment ───────────────────────
            T( 12000, shopId,   "Wireless earbuds",      2026, 6, 13, 15),
            T(  2500, shopId,   "T-shirts × 3",     2026, 6, 20, 14),
            T(  2000, enterId,  "Cinema tickets",        2026, 6,  7, 20),
            T(  2500, healthId, "Doctor visit",          2026, 6, 14, 11),
            T(   650, healthId, "Medicine",              2026, 6, 14, 12),
        )

        rows.forEach { r ->
            txDao.insert(
                Transaction(
                    amount     = r.amt,
                    categoryId = r.cat,
                    note       = r.note,
                    timestamp  = ts(r.y, r.mo, r.d, r.h),
                    source     = "manual",
                    type       = r.type
                )
            )
        }

        // ── Loans ─────────────────────────────────────────────────────────────
        loanDao.insert(Loan(personName = "Ahmed", amount = 8000, direction = "OWED_TO_ME", note = "Medical emergency",         createdAt = ts(2026, 5, 20, 10)))
        loanDao.insert(Loan(personName = "Sara",  amount = 5000, direction = "I_OWE",      note = "Borrowed for Eid shopping", createdAt = ts(2026, 6,  5, 15)))
        val bilalId = loanDao.insert(Loan(personName = "Bilal", amount = 3000, direction = "OWED_TO_ME", note = "Lunch split", createdAt = ts(2026, 5, 15, 12)))
        loanDao.markSettled(bilalId, ts(2026, 6, 25, 14))

        // ── Wishlist ──────────────────────────────────────────────────────────
        wishDao.insert(WishlistItem(name = "iPhone 16 Pro Max",  price = 360000, note = "256GB Natural Titanium", createdAt = ts(2026, 5, 10, 10)))
        wishDao.insert(WishlistItem(name = "Sony PlayStation 5", price = 145000, note = "Slim edition",           createdAt = ts(2026, 5, 18, 15)))
        wishDao.insert(WishlistItem(name = "MacBook Air M3",     price = 285000, note = "15-inch, 16GB RAM",      createdAt = ts(2026, 6,  3, 10)))
        wishDao.insert(WishlistItem(name = "Nike Air Max 270",   price =  22000, note = "Black/White colourway",  createdAt = ts(2026, 6, 12, 12)))
        wishDao.insert(WishlistItem(name = "Sony WH-1000XM5",    price =  68000, note = "ANC headphones",         createdAt = ts(2026, 6, 20, 11)))

        // ── Recurring expenses ────────────────────────────────────────────────
        recDao.insert(RecurringExpense(name = "Netflix",         amount =  1500, categoryId = enterId,  frequency = "MONTHLY", dayOfPeriod =  1, nextRun = nextRun(7,  1)))
        recDao.insert(RecurringExpense(name = "Gym membership",  amount =  3000, categoryId = healthId, frequency = "MONTHLY", dayOfPeriod =  1, nextRun = nextRun(7,  1)))
        recDao.insert(RecurringExpense(name = "Internet bill",   amount =  2000, categoryId = billsId,  frequency = "MONTHLY", dayOfPeriod =  8, nextRun = nextRun(7,  8)))
        recDao.insert(RecurringExpense(name = "Mobile recharge", amount =  1200, categoryId = billsId,  frequency = "MONTHLY", dayOfPeriod = 10, nextRun = nextRun(7, 10)))

        // ── Wallet logs ───────────────────────────────────────────────────────
        walletDao.insertOrUpdate(WalletLog(date = "2026-06-28", reportedBalance = 48500))
        walletDao.insertOrUpdate(WalletLog(date = "2026-06-29", reportedBalance = 46200))
        walletDao.insertOrUpdate(WalletLog(date = "2026-06-30", reportedBalance = 44800))
    }
}
