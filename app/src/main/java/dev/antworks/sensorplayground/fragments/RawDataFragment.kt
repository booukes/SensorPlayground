package dev.antworks.sensorplayground.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import dev.antworks.sensorplayground.R
import dev.antworks.sensorplayground.data.SensorRepository
import dev.antworks.sensorplayground.databinding.FragmentRawDataBinding
import com.google.gson.GsonBuilder

class RawDataFragment : Fragment(R.layout.fragment_raw_data) {

    private var _binding: FragmentRawDataBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRawDataBinding.bind(view)

        updateUiState()

        binding.btnExport.setOnClickListener {
            exportData()
        }

        binding.btnClear.setOnClickListener {
            SensorRepository.rawDataLogs.clear()
            updateUiState()
        }
    }

    private fun updateUiState() {
        if (SensorRepository.rawDataLogs.isEmpty()) {
            binding.tvLogs.text = "No logs recorded yet."
            binding.btnExport.isEnabled = false
            return
        }
        binding.btnExport.isEnabled = true

        if (SensorRepository.dataFormat == "CSV") {
            binding.btnExport.text = "Export CSV"
            binding.tvLogs.text = generateCsvString(preview = true)
        } else {
            binding.btnExport.text = "Export JSON"
            binding.tvLogs.text = generateJsonString(preview = true)
        }
    }

    private fun generateJsonString(preview: Boolean): String {
        val logs = if (preview) SensorRepository.rawDataLogs.takeLast(10) else SensorRepository.rawDataLogs

        val gson = GsonBuilder().setPrettyPrinting().create()
        return gson.toJson(logs)
    }

    private fun generateCsvString(preview: Boolean): String {
        val logs = if (preview) SensorRepository.rawDataLogs.takeLast(20) else SensorRepository.rawDataLogs
        val sb = StringBuilder()

        sb.append("Timestamp,SensorType,Value_1,Value_2,Value_3\n")

        logs.forEach { log ->
            val parts = log.values.split("|")
            val v1 = if (parts.isNotEmpty()) parts[0] else ""
            val v2 = if (parts.size > 1) parts[1] else ""
            val v3 = if (parts.size > 2) parts[2] else ""

            sb.append("${log.timestamp},${log.sensorType},$v1,$v2,$v3\n")
        }
        return sb.toString()
    }

    private fun exportData() {
        val outputContent: String
        val mimeType: String
        val title: String

        if (SensorRepository.dataFormat == "CSV") {
            outputContent = generateCsvString(preview = false)
            mimeType = "text/comma-separated-values"
            title = "Export Sensor Data CSV"
        } else {
            outputContent = generateJsonString(preview = false)
            mimeType = "application/json"
            title = "Export Sensor Data JSON"
        }

        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, outputContent)
            type = mimeType
        }
        startActivity(Intent.createChooser(sendIntent, title))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}