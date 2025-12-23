package pro.maximon.lab3.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timers")
data class TimerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val timer: Long,
    @ColumnInfo(defaultValue = "0") val ticking: Boolean = false,
    @ColumnInfo(defaultValue = "0") val defaultValue: Long = timer,
)
