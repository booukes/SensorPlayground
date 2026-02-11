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

        // Switch preview/output based on selected export format
        if (SensorRepository.dataFormat == "CSV") {
            binding.btnExport.text = "Export CSV"
            binding.tvLogs.text = generateCsvString(preview = true)
        } else {
            binding.btnExport.text = "Export JSON"
            binding.tvLogs.text = generateJsonString(preview = true)
        }
    }

    private fun generateJsonString(preview: Boolean): String {
        // Limit preview size for UI readability
        val logs =
            if (preview) SensorRepository.rawDataLogs.takeLast(10)
            else SensorRepository.rawDataLogs

        val gson = GsonBuilder().setPrettyPrinting().create()
        return gson.toJson(logs)
    }

    private fun generateCsvString(preview: Boolean): String {
        // CSV preview shows more rows since it's compact
        val logs =
            if (preview) SensorRepository.rawDataLogs.takeLast(20)
            else SensorRepository.rawDataLogs

        val sb = StringBuilder()
        sb.append("Timestamp,SensorType,Value_1,Value_2,Value_3\n")

        logs.forEach { log ->
            // Split raw sensor values (x|y|z or single value)
            val parts = log.values.split("|")
            val v1 = parts.getOrElse(0) { "" }
            val v2 = parts.getOrElse(1) { "" }
            val v3 = parts.getOrElse(2) { "" }

            sb.append("${log.timestamp},${log.sensorType},$v1,$v2,$v3\n")
        }
        return sb.toString()
    }

    private fun exportData() {
        val outputContent: String
        val mimeType: String
        val title: String

        // Prepare export payload based on selected format
        if (SensorRepository.dataFormat == "CSV") {
            outputContent = generateCsvString(preview = false)
            mimeType = "text/comma-separated-values"
            title = "Export Sensor Data CSV"
        } else {
            outputContent = generateJsonString(preview = false)
            mimeType = "application/json"
            title = "Export Sensor Data JSON"
        }

        // Share via standard Android intent
        val sendIntent = Intent().apply {
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
