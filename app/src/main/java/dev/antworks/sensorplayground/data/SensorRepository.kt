package dev.antworks.sensorplayground.data

enum class SensorType {
    MAGNETOMETER, ACCELEROMETER, LIGHT
}

object SensorRepository {
    val rawDataLogs = mutableListOf<SensorLog>()

    var selectedSensor: SensorType = SensorType.MAGNETOMETER

    var dataFormat = "JSON"
    var useSqlDatabase = false
}

data class SensorLog(
    val timestamp: Long,
    val sensorType: String,
    val values: String
)