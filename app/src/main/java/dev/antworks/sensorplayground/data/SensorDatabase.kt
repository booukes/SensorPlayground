package dev.antworks.sensorplayground.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SensorLog::class], version = 2)
abstract class SensorDatabase : RoomDatabase() {

    abstract fun logDao(): SensorLogDao

    companion object {
        @Volatile private var INSTANCE: SensorDatabase? = null

        fun get(context: Context): SensorDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context, SensorDatabase::class.java, "sensor_db")
                    .fallbackToDestructiveMigration() // fine for dev
                    .build()
                    .also { INSTANCE = it }
            }
    }
}