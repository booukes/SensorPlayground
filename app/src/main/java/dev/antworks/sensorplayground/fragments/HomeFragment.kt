package dev.antworks.sensorplayground.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dev.antworks.sensorplayground.R
import dev.antworks.sensorplayground.databinding.FragmentHomeBinding

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        binding.btnPlayground.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_playground)
        }

        binding.btnFunGames.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_gamesList)
        }

        binding.btnSensorSettings.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_sensorSettings)
        }

        binding.btnDataSettings.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_dataSettings)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}