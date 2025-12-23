package pro.maximon.lab3.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import pro.maximon.lab3.data.local.TimerDao
import pro.maximon.lab3.data.local.TimerEntity
import pro.maximon.lab3.models.TimerItem
import kotlin.uuid.Uuid

class TimerRepositoryImpl(
    private val timerDao: TimerDao,
): TimerRepository {

    override fun getTimers(): Flow<List<TimerItem>> {
        return timerDao.getAll().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun addTimer(item: TimerItem) {
        withContext(Dispatchers.IO) {
            timerDao.insert(item.toEntity())
        }
    }

    override suspend fun updateTimer(item: TimerItem) {
        withContext(Dispatchers.IO) {
            timerDao.update(item.toEntity())
        }
    }

    override suspend fun removeTimer(id: Uuid) {
        withContext(Dispatchers.IO) {
            timerDao.deleteById(id.toString())
        }
    }

    override suspend fun getTimersOnce(): List<TimerItem> {
        return withContext(Dispatchers.IO) {
            timerDao.getAllOnce().map { it.toDomain() }
        }
    }

    private fun TimerEntity.toDomain(): TimerItem {
        return TimerItem(
            id = Uuid.parse(id),
            name = name,
            timer = timer,
            ticking = ticking,
            defaultValue = defaultValue,
        )
    }

    private fun TimerItem.toEntity(): TimerEntity {
        return TimerEntity(
            id = id.toString(),
            name = name,
            timer = timer,
            ticking = ticking,
            defaultValue = defaultValue,
        )
    }
}