package dev.antworks.sensorplayground.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dev.antworks.sensorplayground.R
import dev.antworks.sensorplayground.databinding.FragmentGamesListBinding

class GamesListFragment : Fragment(R.layout.fragment_games_list) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentGamesListBinding.bind(view)

        binding.btnGameMagnet.setOnClickListener {
            findNavController().navigate(R.id.action_gamesList_to_magneticHunter)
        }
        binding.btnGameTilt.setOnClickListener {
            findNavController().navigate(R.id.action_gamesList_to_tiltGame)
        }
        binding.btnGameLux.setOnClickListener {
            findNavController().navigate(R.id.action_gamesList_to_luxPoc)
        }
    }
}