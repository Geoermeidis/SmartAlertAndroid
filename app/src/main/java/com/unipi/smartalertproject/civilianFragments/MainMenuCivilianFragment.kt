package com.unipi.smartalertproject.civilianFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.unipi.smartalertproject.R
import com.unipi.smartalertproject.api.AuthManager
import com.unipi.smartalertproject.databinding.FragmentMainMenuCivilianBinding

class MainMenuCivilianFragment : Fragment() {
    private var _binding: FragmentMainMenuCivilianBinding? = null
    private lateinit var authManager: AuthManager
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainMenuCivilianBinding.inflate(inflater, container, false)
        authManager = AuthManager(requireContext())
        binding.buttonGoToStats.setOnClickListener {
            findNavController().navigate(R.id.action_mainMenuCivilianFragment_to_incidentStatsPreviewFragment)
        }

        binding.buttonGoToSubmit.setOnClickListener{
            findNavController().navigate(R.id.action_mainMenuCivilianFragment_to_submitIncidentFragment2)
        }

        binding.mapButton.setOnClickListener {
            findNavController().navigate(R.id.action_mainMenuCivilianFragment_to_mapsFragment)
        }

        binding.buttonLogout.setOnClickListener {
            authManager.logout()
            findNavController().navigate(R.id.action_mainMenuCivilianFragment_to_FirstFragment)
        }

        return binding.root
    }

}