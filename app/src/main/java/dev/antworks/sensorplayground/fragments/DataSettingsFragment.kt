package dev.antworks.sensorplayground.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import dev.antworks.sensorplayground.R
import dev.antworks.sensorplayground.data.SensorRepository
import dev.antworks.sensorplayground.databinding.FragmentDataSettingsBinding

class DataSettingsFragment : Fragment(R.layout.fragment_data_settings) {

    private var _binding: FragmentDataSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDataSettingsBinding.bind(view)

        if (SensorRepository.dataFormat == "CSV") {
            binding.rbCsv.isChecked = true
        } else {
            binding.rbJson.isChecked = true
        }
        binding.switchSql.isChecked = SensorRepository.useSqlDatabase

        binding.rgFormat.setOnCheckedChangeListener { _, checkedId ->
            SensorRepository.dataFormat = if (checkedId == R.id.rb_csv) "CSV" else "JSON"
        }

        binding.switchSql.setOnCheckedChangeListener { _, isChecked ->
            SensorRepository.useSqlDatabase = isChecked
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}