package pro.maximon.lab3.service

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import pro.maximon.lab3.data.repository.TimerRepository
import pro.maximon.lab3.models.TimerItem
import pro.maximon.lab3.notifications.TimerNotificationManager

class TimerService : Service() {

    private val timerRepository: TimerRepository by inject()

    private val serviceJob: Job = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)

    private var tickerJob: Job? = null

    private lateinit var notificationManager: TimerNotificationManager

    override fun onCreate() {
        super.onCreate()
        notificationManager = TimerNotificationManager(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification: Notification = notificationManager.buildForegroundNotification(activeTimersCount = null)
        startForeground(TimerNotificationManager.FOREGROUND_NOTIFICATION_ID, notification)

        if (tickerJob == null || tickerJob?.isActive != true) {
            startTicker()
        }

        return START_STICKY
    }

    private fun startTicker() {
        tickerJob = serviceScope.launch {
            while (isActive) {
                try {
                    val timers = timerRepository.getTimersOnce()

                    val updatedTimers = mutableListOf<TimerItem>()

                    for (item in timers) {
                        if (!item.ticking) {
                            continue
                        }

                        if (item.timer <= 0L) {
                            val reset = item.resetToDefault()
                            updatedTimers.add(reset)
                            continue
                        }

                        val decremented = item.copy(timer = (item.timer - 1).coerceAtLeast(0))

                        val finalItem = if (decremented.timer <= 0L) {
                            decremented.resetToDefault()
                        } else {
                            decremented
                        }

                        updatedTimers.add(finalItem)
                    }

                    for (updated in updatedTimers) {
                        timerRepository.updateTimer(updated)
                    }

                    val activeTimersCount = timerRepository
                        .getTimersOnce()
                        .count { it.ticking }

                    notificationManager.updateForegroundNotification(activeTimersCount = activeTimersCount)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                delay(1000L)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tickerJob?.cancel()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}