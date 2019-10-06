package de.mg.androidsave.scheduled

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import de.mg.androidsave.Config.TIMEOUT_SECONDS


object CleanupScheduler {

    fun schedule(context: Context) {

        createOrUpdateChannel(context)
        scheduleCleanup(context)
    }

    private fun createOrUpdateChannel(context: Context) {
        val channel =
            NotificationChannel(channelId(context), "cleanup", NotificationManager.IMPORTANCE_HIGH)
        channel.description = "pw clean"
        channel.setShowBadge(false)

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun channelId(context: Context): String {
        return "${context.packageName}-cleanup"
    }


    private fun scheduleCleanup(context: Context) {
        val intent = Intent(context, CleanupReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.cancel(pendingIntent)
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextExecutionEpochMillis(), pendingIntent)
    }

    private fun nextExecutionEpochMillis(): Long =
        System.currentTimeMillis() + TIMEOUT_SECONDS * 1000

}