package com.nobody.valurex.data.repo

import android.content.Context
import com.nobody.valurex.data.db.dao.ReminderSettingsDao
import com.nobody.valurex.data.db.entity.ReminderSettings
import com.nobody.valurex.notification.ReminderScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull

class ReminderRepository(
    private val dao: ReminderSettingsDao,
    private val context: Context
) {
    fun getSettings(): Flow<ReminderSettings> = dao.getSettings().filterNotNull()

    suspend fun update(settings: ReminderSettings) {
        dao.insertOrUpdate(settings)
        ReminderScheduler.rescheduleAll(context)
    }

    suspend fun updateNightly(enabled: Boolean, hour: Int, minute: Int, current: ReminderSettings) {
        val next = current.copy(
            nightly_enabled = enabled,
            nightly_hour = hour,
            nightly_minute = minute
        )
        update(next)
    }

    suspend fun updateDaily(slot: Int, enabled: Boolean, hour: Int, minute: Int, current: ReminderSettings) {
        val next = when (slot) {
            1 -> current.copy(daily1_enabled = enabled, daily1_hour = hour, daily1_minute = minute)
            2 -> current.copy(daily2_enabled = enabled, daily2_hour = hour, daily2_minute = minute)
            3 -> current.copy(daily3_enabled = enabled, daily3_hour = hour, daily3_minute = minute)
            else -> current
        }
        update(next)
    }
}
