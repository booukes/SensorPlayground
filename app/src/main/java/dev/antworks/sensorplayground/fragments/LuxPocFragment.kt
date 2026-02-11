package dev.antworks.sensorplayground.fragments

import android.content.Context
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import dev.antworks.sensorplayground.R
import dev.antworks.sensorplayground.databinding.FragmentLuxPocBinding

class LuxPocFragment : Fragment(R.layout.fragment_lux_poc), SensorEventListener {

    private var _binding: FragmentLuxPocBinding? = null
    private val binding get() = _binding!!

    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLuxPocBinding.bind(view)

        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    }

    override fun onResume() {
        super.onResume()
        lightSensor?.let {
            // SENSOR_DELAY_UI is enough for quick game, doesnt drain much battery
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            val lux = event.values[0]
            updateUI(lux)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun updateUI(lux: Float) {
        binding.tvLuxValue.text = "%.1f lux".format(lux)

        when {
            lux < 2 -> {
                binding.tvEmoji.text = "😴"
            }
            lux < 50 -> {
                binding.tvEmoji.text = "👀"
            }
            lux < 200 -> {
                binding.tvEmoji.text = "😎"
            }
            else -> {
                binding.tvEmoji.text = "😵"
            }
        }

        // lux mapping to background color
        val brightness = lux.toInt().coerceIn(0, 255)
        val backgroundColor = Color.rgb(brightness, brightness, brightness)
        binding.rootContainer.setBackgroundColor(backgroundColor)
    }

    private fun setTextColor(color: Int) {
        binding.tvEmoji.setTextColor(color)
        binding.tvLuxValue.setTextColor(color)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}