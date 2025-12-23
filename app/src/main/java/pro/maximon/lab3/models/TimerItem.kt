package pro.maximon.lab3.models

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid

data class TimerItem(
    val name: String,
    val id: Uuid = Uuid.random(),
    val timer: Long = 60L,
    val ticking: Boolean = false,
    val defaultValue: Long = timer,
) {
    fun timerDuration(): Duration {
        return timer.seconds
    }

    fun decrementSecond(): TimerItem {
        val newTimer = if (timer > 0L) timer - 1 else 0L
        return this.copy(timer = newTimer)
    }

    fun isFinished(): Boolean = timer <= 0L

    fun resetToDefault(): TimerItem = copy(timer = defaultValue, ticking = false)
}
