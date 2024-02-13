package com.unipi.smartalertproject.officerFragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.unipi.smartalertproject.R
import com.unipi.smartalertproject.api.ApiService
import com.unipi.smartalertproject.api.AuthManager
import com.unipi.smartalertproject.api.Incident
import com.unipi.smartalertproject.api.IncidentAPIResponse
import com.unipi.smartalertproject.api.RetrofitClient
import com.unipi.smartalertproject.api.Utils
import com.unipi.smartalertproject.databinding.FragmentFirstBinding
import com.unipi.smartalertproject.databinding.FragmentIncidentStatsPreviewBinding
import com.unipi.smartalertproject.databinding.FragmentMainMenuOfficerBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainMenuOfficerFragment : Fragment() {
    private val apiService = RetrofitClient.retrofit.create(ApiService::class.java)
    private var authManager: AuthManager? = null
    private var _binding: FragmentMainMenuOfficerBinding? = null

    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMainMenuOfficerBinding.inflate(inflater, container, false)

        binding.buttonGoToMap.setOnClickListener {
            findNavController().navigate(R.id.action_mainMenuOfficerFragment_to_mapsFragment)
        }

        binding.buttonGoToSubmissions.setOnClickListener {
            findNavController().navigate(R.id.action_mainMenuOfficerFragment_to_incidentsPreviewFragment2)
        }

        return binding.root
    }


}