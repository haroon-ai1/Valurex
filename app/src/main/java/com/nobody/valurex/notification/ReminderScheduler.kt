package com.nobody.valurex.notification

import android.content.Context
import androidx.work.*
import com.nobody.valurex.ValurexApplication
import com.nobody.valurex.data.db.entity.ReminderSettings
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import java.util.*
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    private const val TAG_NIGHTLY = "reminder_nightly"
    private const val TAG_DAILY1 = "reminder_daily_1"
    private const val TAG_DAILY2 = "reminder_daily_2"
    private const val TAG_DAILY3 = "reminder_daily_3"

    fun rescheduleAll(context: Context) {
        val app = context.applicationContext as ValurexApplication
        val settings = runBlocking { app.reminderRepository.getSettings().firstOrNull() } ?: return

        cancelAll(context)

        if (settings.nightly_enabled) {
            schedule(context, TAG_NIGHTLY, settings.nightly_hour, settings.nightly_minute, NightlyCheckinWorker::class.java)
        }
        if (settings.daily1_enabled) {
            schedule(context, TAG_DAILY1, settings.daily1_hour, settings.daily1_minute, DailyReminderWorker::class.java)
        }
        if (settings.daily2_enabled) {
            schedule(context, TAG_DAILY2, settings.daily2_hour, settings.daily2_minute, DailyReminderWorker::class.java)
        }
        if (settings.daily3_enabled) {
            schedule(context, TAG_DAILY3, settings.daily3_hour, settings.daily3_minute, DailyReminderWorker::class.java)
        }
    }

    fun cancelAll(context: Context) {
        val wm = WorkManager.getInstance(context)
        wm.cancelAllWorkByTag(TAG_NIGHTLY)
        wm.cancelAllWorkByTag(TAG_DAILY1)
        wm.cancelAllWorkByTag(TAG_DAILY2)
        wm.cancelAllWorkByTag(TAG_DAILY3)
    }

    private fun <T : ListenableWorker> schedule(
        context: Context,
        tag: String,
        hour: Int,
        minute: Int,
        workerClass: Class<T>
    ) {
        val delay = calculateDelay(hour, minute)
        val request = OneTimeWorkRequest.Builder(workerClass)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag(tag)
            .build()
        WorkManager.getInstance(context).enqueue(request)
    }

    private fun calculateDelay(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (target.before(now)) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }
        return target.timeInMillis - now.timeInMillis
    }
}
