package pro.maximon.lab3.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TimerDao {
    @Query("SELECT * FROM timers")
    fun getAll(): Flow<List<TimerEntity>>

    @Query("SELECT * FROM timers")
    suspend fun getAllOnce(): List<TimerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(timer: TimerEntity)

    @Update
    suspend fun update(timer: TimerEntity)

    @Delete
    suspend fun delete(timer: TimerEntity)

    @Query("DELETE FROM timers WHERE id = :id")
    suspend fun deleteById(id: String)
}