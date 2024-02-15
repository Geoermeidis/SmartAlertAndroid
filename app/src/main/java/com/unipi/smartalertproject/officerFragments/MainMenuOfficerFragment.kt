package com.unipi.smartalertproject.officerFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.unipi.smartalertproject.R
import com.unipi.smartalertproject.api.AuthManager
import com.unipi.smartalertproject.databinding.FragmentMainMenuOfficerBinding

class MainMenuOfficerFragment : Fragment() {
    private var _binding: FragmentMainMenuOfficerBinding? = null
    private lateinit var authManager: AuthManager
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMainMenuOfficerBinding.inflate(inflater, container, false)
        authManager = AuthManager(requireContext())
        binding.buttonGoToMap.setOnClickListener {
            findNavController().navigate(R.id.action_mainMenuOfficerFragment_to_mapsFragment)
        }

        binding.buttonGoToSubmissions.setOnClickListener {
            findNavController().navigate(R.id.action_mainMenuOfficerFragment_to_incidentsPreviewFragment2)
        }

        binding.buttonLogout.setOnClickListener {
            authManager.logout()
            findNavController().navigate(R.id.action_mainMenuOfficerFragment_to_FirstFragment)
        }

        return binding.root
    }


}