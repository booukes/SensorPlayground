package dev.antworks.sensorplayground.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import dev.antworks.sensorplayground.R
import dev.antworks.sensorplayground.data.SensorDatabase
import dev.antworks.sensorplayground.data.SensorLog
import dev.antworks.sensorplayground.data.SensorRepository
import dev.antworks.sensorplayground.databinding.FragmentRawDataBinding
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RawDataFragment : Fragment(R.layout.fragment_raw_data) {

    private var _binding: FragmentRawDataBinding? = null
    private val binding get() = _binding!!

    // If opened from SessionsFragment, this will be set to a specific session ID.
    // If opened from PlaygroundFragment (View Logs button), it's -1 → show in-memory list.
    private var sessionId: Long = -1L

    // The logs currently displayed — either in-memory or loaded from DB
    private var displayedLogs: List<SensorLog> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRawDataBinding.bind(view)

        sessionId = arguments?.getLong("sessionId", -1L) ?: -1L

        lifecycleScope.launch {
            loadLogs()
            updateUiState()
        }

        binding.btnExport.setOnClickListener { exportData() }

        binding.btnClear.setOnClickListener {
            lifecycleScope.launch {
                if (sessionId != -1L) {
                    // Viewing a specific DB session — delete just that session
                    withContext(Dispatchers.IO) {
                        SensorDatabase.get(requireContext()).logDao().deleteSession(sessionId)
                    }
                    displayedLogs = emptyList()
                } else if (SensorRepository.useSqlDatabase) {
                    withContext(Dispatchers.IO) {
                        SensorDatabase.get(requireContext()).logDao().clearAll()
                    }
                    SensorRepository.rawDataLogs.clear()
                    displayedLogs = emptyList()
                } else {
                    SensorRepository.rawDataLogs.clear()
                    displayedLogs = emptyList()
                }
                updateUiState()
            }
        }
    }

    private suspend fun loadLogs() {
        displayedLogs = when {
            sessionId != -1L -> {
                withContext(Dispatchers.IO) {
                    SensorDatabase.get(requireContext()).logDao().getSession(sessionId)
                }
            }
            SensorRepository.useSqlDatabase -> {
                withContext(Dispatchers.IO) {
                    val dao = SensorDatabase.get(requireContext()).logDao()
                    val allIds = dao.getAllSessionIds()
                    allIds.flatMap { dao.getSession(it) }
                }.also {
                    SensorRepository.rawDataLogs.apply { clear(); addAll(it) }
                }
            }
            else -> {
                SensorRepository.rawDataLogs.toList()
            }
        }
    }

    private fun updateUiState() {
        if (displayedLogs.isEmpty()) {
            binding.tvLogs.text = "No logs recorded yet."
            binding.btnExport.isEnabled = false
            return
        }

        binding.btnExport.isEnabled = true

        if (SensorRepository.dataFormat == "CSV") {
            binding.btnExport.text = "Export CSV"
            binding.tvLogs.text = toCsv(displayedLogs.takeLast(20))
        } else {
            binding.btnExport.text = "Export JSON"
            binding.tvLogs.text = toJson(displayedLogs.takeLast(10))
        }
    }

    private fun toJson(logs: List<SensorLog>): String =
        GsonBuilder().setPrettyPrinting().create().toJson(logs)

    private fun toCsv(logs: List<SensorLog>): String = buildString {
        append("Timestamp,SensorType,Value_1,Value_2,Value_3\n")
        logs.forEach { log ->
            val p = log.values.split("|")
            append("${log.timestamp},${log.sensorType},${p.getOrElse(0){""}}," +
                    "${p.getOrElse(1){""}}," +
                    "${p.getOrElse(2){""}}\n")
        }
    }

    private fun exportData() {
        val (content, mime, title) = if (SensorRepository.dataFormat == "CSV") {
            Triple(toCsv(displayedLogs), "text/comma-separated-values", "Export Sensor CSV")
        } else {
            Triple(toJson(displayedLogs), "application/json", "Export Sensor JSON")
        }
        startActivity(Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply { putExtra(Intent.EXTRA_TEXT, content); type = mime },
            title
        ))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}