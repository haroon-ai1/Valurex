package com.nobody.valurex.data.repo

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class SavingsBarPrefs(context: Context) {
    private val prefs = context.getSharedPreferences("valurex_savings", Context.MODE_PRIVATE)

    private val _dismissedWeek = MutableStateFlow(prefs.getLong("savings_bar_dismissed_week", 0L))
    val dismissedWeekFlow: StateFlow<Long> = _dismissedWeek.asStateFlow()

    suspend fun dismissForWeek(weekStartMillis: Long) {
        withContext(Dispatchers.IO) {
            prefs.edit().putLong("savings_bar_dismissed_week", weekStartMillis).commit()
        }
        _dismissedWeek.value = weekStartMillis
    }
}
