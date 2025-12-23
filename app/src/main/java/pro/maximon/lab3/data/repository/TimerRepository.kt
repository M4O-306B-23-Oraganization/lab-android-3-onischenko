package pro.maximon.lab3.data.repository

import kotlinx.coroutines.flow.Flow
import pro.maximon.lab3.models.TimerItem
import kotlin.uuid.Uuid

interface TimerRepository {
    fun getTimers(): Flow<List<TimerItem>>
    suspend fun addTimer(item: TimerItem)
    suspend fun updateTimer(item: TimerItem)
    suspend fun removeTimer(id: Uuid)
    suspend fun getTimersOnce(): List<TimerItem>
}