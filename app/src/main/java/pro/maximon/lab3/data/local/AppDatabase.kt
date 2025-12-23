package pro.maximon.lab3.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [TimerEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase: RoomDatabase() {

    abstract fun timerDao(): TimerDao

    companion object {
        @Volatile
        private var _instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return _instance ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "timers.db",
                )
                    .fallbackToDestructiveMigration()
                    .build()
                _instance = instance
                instance
            }
        }
    }
}