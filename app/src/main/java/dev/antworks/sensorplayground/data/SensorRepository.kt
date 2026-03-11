package dev.antworks.sensorplayground.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Collections

enum class SensorType {
    MAGNETOMETER, ACCELEROMETER, LIGHT
}

object SensorRepository {
    val rawDataLogs: MutableList<SensorLog> =
        Collections.synchronizedList(mutableListOf())

    var selectedSensor: SensorType = SensorType.MAGNETOMETER
    var dataFormat = "JSON"
    var useSqlDatabase = true
}

@Entity(tableName = "sensor_logs")
data class SensorLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val sensorType: String,
    val values: String,
    val sessionId: Long = 0L
)