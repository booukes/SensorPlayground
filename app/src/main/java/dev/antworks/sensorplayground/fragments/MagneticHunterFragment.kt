package dev.antworks.sensorplayground.fragments

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import dev.antworks.sensorplayground.R
import dev.antworks.sensorplayground.databinding.FragmentMagneticHunterBinding
import kotlin.math.sqrt

class MagneticHunterFragment : Fragment(R.layout.fragment_magnetic_hunter), SensorEventListener {

    private var _binding: FragmentMagneticHunterBinding? = null
    private val binding get() = _binding!!

    private lateinit var sensorManager: SensorManager
    private var magnetometer: Sensor? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMagneticHunterBinding.bind(view)

        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        if (magnetometer == null) {
            // Device does not support magnetometer
            binding.tvStatus.text = "NO SENSOR :("
            binding.tvStatus.setTextColor(
                ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)
            )
        }
    }

    override fun onResume() {
        super.onResume()
        // Register listener only if sensor is available
        magnetometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onPause() {
        super.onPause()
        // unregister to avoid leaks and unnecessary battery usage
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                val x = it.values[0]
                val y = it.values[1]
                val z = it.values[2]

                // Calculate total magnetic field strength (uT)
                val magnitude = sqrt((x * x) + (y * y) + (z * z))
                updateGameUI(magnitude)
            }
        }
    }

    private fun updateGameUI(magnitude: Float) {
        val score = magnitude.toInt()
        binding.tvScore.text = "$score µT"
        binding.progressStrength.progress = score

        when {
            score > 200 -> {
                binding.tvStatus.text = "☢️ DANGER! ☢️"
                binding.tvStatus.setTextColor(0xFFFF0000.toInt())
                binding.root.setBackgroundColor(0xFF220000.toInt())
            }
            (score !in 30..70) -> {
                binding.tvStatus.text = "FOUND METAL!"
                binding.tvStatus.setTextColor(0xFFFF8800.toInt())
                binding.root.setBackgroundColor(0xFF121212.toInt())
            }
            else -> {
                binding.tvStatus.text = "SCANNING..."
                binding.tvStatus.setTextColor(0xFF00FF00.toInt())
                binding.root.setBackgroundColor(0xFF121212.toInt())
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
