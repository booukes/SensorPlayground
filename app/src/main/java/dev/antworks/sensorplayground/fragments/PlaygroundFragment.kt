package dev.antworks.sensorplayground.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dev.antworks.sensorplayground.R
import dev.antworks.sensorplayground.api.RetrofitClient
import dev.antworks.sensorplayground.data.SensorLog
import dev.antworks.sensorplayground.data.SensorRepository
import dev.antworks.sensorplayground.data.SensorType
import dev.antworks.sensorplayground.databinding.FragmentPlaygroundBinding
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.sqrt

class PlaygroundFragment : Fragment(R.layout.fragment_playground), SensorEventListener {

    private var _binding: FragmentPlaygroundBinding? = null
    // Valid only between onViewCreated and onDestroyView
    private val binding get() = _binding!!

    private lateinit var sensorManager: SensorManager
    private var activeSensor: Sensor? = null
    private var currentSensorType: SensorType = SensorType.MAGNETOMETER

    private val entries = ArrayList<Entry>()
    private var startTime = System.currentTimeMillis()
    private var isRecording = false

    // Expected Earth magnetic field from API (µT)
    private var expectedFieldStrength: Double? = null
    private var currentMagnitude: Float = 0f

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPlaygroundBinding.bind(view)

        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager

        currentSensorType = SensorRepository.selectedSensor
        setupActiveSensor()
        setupChart()

        binding.btnSyncApi.setOnClickListener { fetchApiData() }

        binding.btnViewRawData.setOnClickListener {
            findNavController().navigate(R.id.action_playground_to_rawData)
        }

        binding.btnRecord.setOnCheckedChangeListener { _, isChecked ->
            isRecording = isChecked
            Toast.makeText(
                context,
                if (isChecked) "Recording started" else "Recording stopped",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setupActiveSensor() {
        val sensorIntType: Int
        val sensorName: String

        // UI + sensor selection based on chosen sensor type
        if (currentSensorType == SensorType.MAGNETOMETER) {
            binding.tvApiData.visibility = View.VISIBLE
            binding.btnSyncApi.visibility = View.VISIBLE
            sensorIntType = Sensor.TYPE_MAGNETIC_FIELD
            sensorName = "Magnetometer (µT)"
        } else if (currentSensorType == SensorType.ACCELEROMETER) {
            binding.tvApiData.visibility = View.GONE
            binding.btnSyncApi.visibility = View.GONE
            sensorIntType = Sensor.TYPE_ACCELEROMETER
            sensorName = "Accelerometer (m/s²)"
        } else {
            binding.tvApiData.visibility = View.GONE
            binding.btnSyncApi.visibility = View.GONE
            sensorIntType = Sensor.TYPE_LIGHT
            sensorName = "Light Sensor (Lux)"
        }

        activeSensor = sensorManager.getDefaultSensor(sensorIntType)

        binding.tvSensorData.text =
            if (activeSensor == null) "Sensor Not Supported on Device"
            else "Waiting for data..."

        // Update dataset label if chart already exists
        binding.chartSensor.data?.takeIf { it.dataSetCount > 0 }
            ?.getDataSetByIndex(0)?.label = sensorName
    }

    private fun setupChart() {
        val label = when (currentSensorType) {
            SensorType.MAGNETOMETER -> "Mag Field (µT)"
            SensorType.ACCELEROMETER -> "Accel (m/s²)"
            SensorType.LIGHT -> "Light (Lux)"
        }

        val dataSet = LineDataSet(entries, label).apply {
            setDrawCircles(false)
            lineWidth = 2f
            color = requireContext().getColor(android.R.color.holo_red_dark)
            setDrawValues(false)
        }

        binding.chartSensor.data = LineData(dataSet)
        binding.chartSensor.description.isEnabled = false
        binding.chartSensor.invalidate()
    }

    override fun onResume() {
        super.onResume()
        activeSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        var plottedValue = 0f
        var logValues = ""

        when (currentSensorType) {
            SensorType.LIGHT -> {
                val lux = event.values[0]
                plottedValue = lux
                logValues = "$lux"
                binding.tvSensorData.text = "Light: %.1f Lux".format(lux)
            }

            SensorType.MAGNETOMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                // Vector magnitude of magnetic field
                val magnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                currentMagnitude = magnitude

                plottedValue = magnitude
                logValues = "$x|$y|$z"

                binding.tvSensorData.text =
                    expectedFieldStrength?.let { expected ->
                        val diff = magnitude - expected
                        val diffPercent = abs(diff / expected) * 100
                        "Sensor: %.2f µT (%.1f%% ${if (diff > 0) "above" else "below"} expected)"
                            .format(magnitude, diffPercent)
                    } ?: "Sensor: %.2f µT".format(magnitude)
            }

            SensorType.ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                val magnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                plottedValue = magnitude
                logValues = "$x|$y|$z"
                binding.tvSensorData.text = "Accel: %.2f m/s²".format(magnitude)
            }
        }

        val timeSec = (System.currentTimeMillis() - startTime) / 1000f
        addEntryToChart(timeSec, plottedValue)

        // Persist raw values if recording is enabled
        if (isRecording) {
            SensorRepository.rawDataLogs.add(
                SensorLog(System.currentTimeMillis(), currentSensorType.name, logValues)
            )
        }
    }

    private fun addEntryToChart(x: Float, y: Float) {
        val data = binding.chartSensor.data ?: return
        var set = data.getDataSetByIndex(0)

        if (set == null) {
            set = LineDataSet(ArrayList(), "Data").apply {
                setDrawCircles(false)
                lineWidth = 2f
                color = requireContext().getColor(android.R.color.holo_red_dark)
                setDrawValues(false)
            }
            data.addDataSet(set)
        }

        data.addEntry(Entry(x, y), 0)

        // Keep dataset size bounded
        if (set.entryCount > 500) {
            set.removeFirst()
        }

        data.notifyDataChanged()
        binding.chartSensor.notifyDataSetChanged()
        binding.chartSensor.setVisibleXRangeMaximum(10f)
        binding.chartSensor.moveViewToX(x)
    }

    private fun fetchApiData() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                101
            )
            return
        }

        val locManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val location =
            locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        if (location == null) {
            Toast.makeText(context, "Location unavailable", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val currentDate = dateFormat.format(Calendar.getInstance().time)

                val response = RetrofitClient.instance.getMagneticField(
                    model = "wmm",
                    revision = "2025",
                    lat = location.latitude,
                    lon = location.longitude,
                    alt = 0.0,
                    date = currentDate,
                    format = "json"
                )

                if (!response.isSuccessful || response.body() == null) {
                    binding.tvApiData.text = "API Error: ${response.code()}"
                    return@launch
                }

                val totalIntensity =
                    response.body()!!.result?.fieldValue?.totalIntensity?.value
                        ?: run {
                            binding.tvApiData.text = "API: No data"
                            return@launch
                        }

                val uT = totalIntensity / 1000.0
                expectedFieldStrength = uT

                val diff = currentMagnitude - uT
                val diffPercent = abs(diff / uT) * 100

                binding.tvApiData.text = "Expected: %.2f µT\n%s%s".format(
                    uT,
                    when {
                        diffPercent < 5 -> "✓ Accurate"
                        diffPercent < 15 -> "~ Minor interference"
                        else -> "⚠ Strong interference detected"
                    },
                    if (abs(diff) > 5)
                        " | ${if (diff > 0) "+" else ""}%.1f µT".format(diff)
                    else ""
                )

            } catch (e: Exception) {
                binding.tvApiData.text = "Net Error"
                e.printStackTrace()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
