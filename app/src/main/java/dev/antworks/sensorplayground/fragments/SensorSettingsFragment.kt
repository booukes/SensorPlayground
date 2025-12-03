package dev.antworks.sensorplayground.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import dev.antworks.sensorplayground.R
import dev.antworks.sensorplayground.data.SensorRepository
import dev.antworks.sensorplayground.data.SensorType
import dev.antworks.sensorplayground.databinding.FragmentSensorSettingsBinding

class SensorSettingsFragment : Fragment(R.layout.fragment_sensor_settings) {

    private var _binding: FragmentSensorSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSensorSettingsBinding.bind(view)

        when (SensorRepository.selectedSensor) {
            SensorType.MAGNETOMETER -> binding.rbMagnetometer.isChecked = true
            SensorType.ACCELEROMETER -> binding.rbAccelerometer.isChecked = true
            SensorType.LIGHT -> binding.rbLight.isChecked = true
        }

        binding.rgSensors.setOnCheckedChangeListener { _, checkedId ->
            SensorRepository.selectedSensor = when (checkedId) {
                R.id.rb_accelerometer -> SensorType.ACCELEROMETER
                R.id.rb_light -> SensorType.LIGHT
                else -> SensorType.MAGNETOMETER
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}