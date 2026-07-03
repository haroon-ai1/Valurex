package com.nobody.valurex.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nobody.valurex.MainActivity
import com.nobody.valurex.R
import com.nobody.valurex.ValurexApplication

class DailyReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): androidx.work.ListenableWorker.Result {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(applicationContext, "valurex_reminders")
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setContentTitle("Don't forget to log!")
            .setContentText("Quick — add your expenses for today.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(applicationContext).notify(System.currentTimeMillis().toInt(), builder.build())
        } catch (e: SecurityException) {
            // No permission
        }

        // Re-enqueue
        ReminderScheduler.rescheduleAll(applicationContext)

        return androidx.work.ListenableWorker.Result.success()
    }
}
