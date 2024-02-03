package com.unipi.smartalertproject.CivilianFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.unipi.smartalertproject.R
import com.unipi.smartalertproject.Utils
import com.unipi.smartalertproject.api.ApiService
import com.unipi.smartalertproject.api.AuthManager
import com.unipi.smartalertproject.api.RetrofitClient
import com.unipi.smartalertproject.databinding.FragmentFirstBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SubmitIncidentFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SubmitIncidentFragment : Fragment() {
    private val apiService = RetrofitClient.retrofit.create(ApiService::class.java)
    private var _binding: FragmentFirstBinding? = null
    private var authManager: AuthManager? = null
    private val utils: Utils = Utils()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        authManager = AuthManager(requireContext())
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}