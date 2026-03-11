package dev.antworks.sensorplayground.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.antworks.sensorplayground.R
import dev.antworks.sensorplayground.data.SensorDatabase
import dev.antworks.sensorplayground.databinding.FragmentSessionsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SessionsFragment : Fragment(R.layout.fragment_sessions) {

    private var _binding: FragmentSessionsBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSessionsBinding.bind(view)

        binding.rvSessions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSessions.addItemDecoration(
            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        )

        loadSessions()
    }

    private fun loadSessions() {
        lifecycleScope.launch {
            val sessionIds = withContext(Dispatchers.IO) {
                SensorDatabase.get(requireContext()).logDao().getAllSessionIds()
            }

            if (sessionIds.isEmpty()) {
                binding.tvEmpty.visibility = View.VISIBLE
                binding.rvSessions.visibility = View.GONE
                return@launch
            }

            binding.tvEmpty.visibility = View.GONE
            binding.rvSessions.visibility = View.VISIBLE

            val sessionCounts = withContext(Dispatchers.IO) {
                val dao = SensorDatabase.get(requireContext()).logDao()
                sessionIds.associateWith { dao.getSession(it).size }
            }

            binding.rvSessions.adapter = SessionsAdapter(
                sessions = sessionIds,
                counts = sessionCounts,
                onOpen = { sessionId ->
                    findNavController().navigate(
                        R.id.action_sessions_to_rawData,
                        bundleOf("sessionId" to sessionId)
                    )
                },
                onDelete = { sessionId ->
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            SensorDatabase.get(requireContext()).logDao().deleteSession(sessionId)
                        }
                        loadSessions()
                    }
                }
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

private class SessionsAdapter(
    private val sessions: List<Long>,
    private val counts: Map<Long, Int>,
    private val onOpen: (Long) -> Unit,
    private val onDelete: (Long) -> Unit
) : RecyclerView.Adapter<SessionsAdapter.VH>() {

    private val fmt = SimpleDateFormat("dd MMM yyyy  HH:mm:ss", Locale.getDefault())

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val date: TextView = view.findViewById(R.id.tv_session_date)
        val count: TextView = view.findViewById(R.id.tv_session_count)
        val delete: Button = view.findViewById(R.id.btn_delete_session)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_session, parent, false)
        return VH(view)
    }

    override fun getItemCount() = sessions.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val sessionId = sessions[position]
        holder.date.text = fmt.format(Date(sessionId))
        holder.count.text = "${counts[sessionId] ?: 0} data points"
        holder.itemView.setOnClickListener { onOpen(sessionId) }
        holder.delete.setOnClickListener { onDelete(sessionId) }
    }
}