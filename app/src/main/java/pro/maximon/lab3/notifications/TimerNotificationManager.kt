package pro.maximon.lab3.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.core.app.NotificationCompat
import pro.maximon.lab3.MainActivity
import pro.maximon.lab3.R

class TimerNotificationManager(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "timer_service_channel"
        const val CHANNEL_NAME = "Timer service"
        const val FOREGROUND_NOTIFICATION_ID = 1
    }

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createChannelIfNeeded()
    }

    private fun createChannelIfNeeded() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Фоновые таймеры"
            enableLights(false)
            enableVibration(false)
            lightColor = Color.BLUE
            setShowBadge(false)
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun buildContentText(activeTimersCount: Int?): String {
        return if (activeTimersCount == null) {
            "Фоновый сервис запущен"
        } else if (activeTimersCount <= 0) {
            "Нет активных таймеров"
        } else {
            "Активных таймеров: $activeTimersCount"
        }
    }

    fun buildForegroundNotification(activeTimersCount: Int?): Notification {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            pendingIntentFlags
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(buildContentText(activeTimersCount))
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    fun updateForegroundNotification(activeTimersCount: Int?) {
        val notification = buildForegroundNotification(activeTimersCount)
        notificationManager.notify(FOREGROUND_NOTIFICATION_ID, notification)
    }
}