package dev.antworks.sensorplayground.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SensorLogDao {

    @Insert
    suspend fun insert(log: SensorLog)

    // All logs for a specific session
    @Query("SELECT * FROM sensor_logs WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getSession(sessionId: Long): List<SensorLog>

    // Distinct session IDs ordered newest first — used to populate the sessions list
    @Query("SELECT DISTINCT sessionId FROM sensor_logs ORDER BY sessionId DESC")
    suspend fun getAllSessionIds(): List<Long>

    @Query("DELETE FROM sensor_logs WHERE sessionId = :sessionId")
    suspend fun deleteSession(sessionId: Long)

    @Query("DELETE FROM sensor_logs")
    suspend fun clearAll()
}