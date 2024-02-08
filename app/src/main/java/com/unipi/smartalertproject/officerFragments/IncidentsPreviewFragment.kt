package com.unipi.smartalertproject.officerFragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.unipi.smartalertproject.R
import com.unipi.smartalertproject.api.APIResponse
import com.unipi.smartalertproject.api.ApiService
import com.unipi.smartalertproject.api.AuthManager
import com.unipi.smartalertproject.api.Incident
import com.unipi.smartalertproject.api.IncidentAPIResponse
import com.unipi.smartalertproject.api.RetrofitClient
import com.unipi.smartalertproject.api.Utils
import com.unipi.smartalertproject.databinding.FragmentIncidentsPreviewBinding
import com.unipi.smartalertproject.helperFragments.IncidentInfoDialogFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class IncidentsPreviewFragment : Fragment() {
    private val apiService = RetrofitClient.retrofit.create(ApiService::class.java)

    private var _binding: FragmentIncidentsPreviewBinding? = null
    private val binding get() = _binding!!
    private var authManager: AuthManager? = null
    private val utils: Utils = Utils()

    // View model (in a way)
    private var incidents: List<Incident> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment\
        _binding = FragmentIncidentsPreviewBinding.inflate(inflater, container, false)
        authManager = AuthManager(requireContext())

        getIncidents()

        binding.buttonUpdateTable.setOnClickListener {
            binding.tableIncidents.removeViews(1, binding.tableIncidents.childCount-1)
            incidents = emptyList()
            getIncidents()
        }

        binding.buttonOpenMapsView.setOnClickListener{
            val bundle = Bundle()
            bundle.putParcelableArray("incidents", incidents.toTypedArray())
            findNavController().navigate(R.id.action_incidentsPreviewFragment2_to_mapsFragment,
                bundle)
        }

        return binding.root
    }

    private fun fillTable(){
        // get only submitted incidents
        incidents.sortedByDescending { incident -> incident.totalSubmissions }
            .filter { incident: Incident -> incident.state == 0 }
            .forEach { incident ->
            val density = resources.displayMetrics.density
            val padding = (2 * density + 0.5f).toInt()

            val categoryView = TextView(requireContext()).apply {
                text = incident.categoryName.replaceFirstChar {cat ->
                    if (cat.isLowerCase()) cat.titlecase(Locale.getDefault()) else cat.toString()
                }
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                setPadding(padding, padding, padding, padding)
            }

            val submissionsView = TextView(requireContext()).apply {
                text = incident.totalSubmissions.toString()
                setPadding(padding, padding, padding, padding)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                textAlignment = View.TEXT_ALIGNMENT_CENTER
            }

            val infoBtn = ImageButton(requireContext()).apply {
                setImageResource(R.drawable.info_icon)
                scaleType = ImageView.ScaleType.FIT_XY
                layoutParams = TableRow.LayoutParams((60 * density + 0.5f).toInt(),
                    (50 * density + 0.5f).toInt()).apply {
                    setMargins(
                        (5 * density + 0.5f).toInt(),
                        (3 * density + 0.5f).toInt(),
                        (2 * density + 0.5f).toInt(),
                        (3 * density + 0.5f).toInt()
                    )
                }
                setPadding(padding, padding, padding, padding)
                contentDescription = "Information"
                setOnClickListener {
                    val bundle = bundleOf("incident" to incident)
                    val infoDialog = IncidentInfoDialogFragment()
                    infoDialog.arguments = bundle
                    infoDialog.show(childFragmentManager, "InfoDialogFragment")
                }
            }

            val rejectBtn = ImageButton(requireContext()).apply {
                setImageResource(R.drawable.rejected_icon)
                scaleType = ImageView.ScaleType.FIT_XY
                layoutParams = TableRow.LayoutParams((60 * density + 0.5f).toInt(),
                    (50 * density + 0.5f).toInt()).apply {
                    setMargins(
                        (2 * density + 0.5f).toInt(),
                        (3 * density + 0.5f).toInt(),
                        (2 * density + 0.5f).toInt(),
                        (3 * density + 0.5f).toInt()
                    )
                }
                setPadding(padding, padding, padding, padding)
                contentDescription = "Reject"
                setOnClickListener {
                    Log.i("Rott view", this.toString())
                    changeIncidentStatus(incident.id, "rejected", this.parent as View)
                }
            }

            val acceptBtn = ImageButton(requireContext()).apply {
                setImageResource(R.drawable.accepted_icon)
                scaleType = ImageView.ScaleType.FIT_XY
                layoutParams = TableRow.LayoutParams((60 * density + 0.5f).toInt(),
                    (50 * density + 0.5f).toInt()).apply {
                    setMargins(
                        (2 * density + 0.5f).toInt(),
                        (3 * density + 0.5f).toInt(),
                        (2 * density + 0.5f).toInt(),
                        (3 * density + 0.5f).toInt()
                    )
                }

                setPadding(padding, padding, padding, padding)
                contentDescription = "Accept"
                setOnClickListener {
                    changeIncidentStatus(incident.id, "accepted", this.parent as View)
                }
            }

            val divider = View(requireContext()).apply {
                layoutParams =  TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    (8 * density + 0.5f).toInt()) // Height in pixels
                setBackgroundColor(Color.GRAY)
            }

            val tableRow = TableRow(requireContext()).apply {
                addView(categoryView)
                addView(submissionsView)
                addView(infoBtn)
                addView(acceptBtn)
                addView(rejectBtn)
            }

            // todo fix geocoder
            // todo add map with icons and pointers
            // todo add info pop up
            // todo add images instead of buttons/images on buttons

            binding.tableIncidents.addView(tableRow)
            binding.tableIncidents.addView(divider)
        }
    }

    private fun getIncidents(){
        val token = authManager?.getAccessToken()
        if (token != null && authManager != null){
            binding.progressBar2.visibility = View.VISIBLE
            val call: Call<IncidentAPIResponse> = apiService.getIncidents("Bearer $token")
            // execute call and wait for response or fail
            call.enqueue(object : Callback<IncidentAPIResponse> {
                override fun onResponse(call: Call<IncidentAPIResponse>, response: Response<IncidentAPIResponse>) {
                    if (response.isSuccessful) {
                        val data = response.body()
                        incidents  = data?.result!!
                        fillTable()
                        binding.progressBar2.visibility = View.INVISIBLE
                        Log.e("Incidents", "Incidents acquired")
                    }
                    else {
                        // Access token has expired so we must refresh it
                        if (authManager!!.isAccessTokenExpired(token)){
                            Log.e("Token expiration", "Token expired")
                            authManager!!.refreshToken(apiService)
                            binding.progressBar2.visibility = View.INVISIBLE
                        }
                        Log.e("Incidents error code", response.code().toString())
                        Log.e("Incidents error", response.message().toString())
                        // You can check response.code() and response.message() for details
                    }
                }

                override fun onFailure(call: Call<IncidentAPIResponse>, t: Throwable) {
                    // Handle failure here
                    Log.e("Api error",  t.message.toString())
                }
            })
        }
    }

    private fun changeIncidentStatus(id: String, state: String, view: View){
        binding.progressBar2.visibility = View.VISIBLE
        val token = authManager?.getAccessToken()
        if (token != null){
            // create call and send authorization token
            val call: Call<APIResponse> = apiService.processIncident("Bearer $token",
                id, state)

            // execute call and wait for response or fail
            call.enqueue(object : Callback<APIResponse> {

                override fun onResponse(call: Call<APIResponse>, response: Response<APIResponse>) {
                    Log.i("Call", "Call responded")
                    if (response.isSuccessful) { // Handle successful response here
                        utils.showSuccessMessage("Incident $state successfully",
                            Toast.LENGTH_LONG, requireContext())
                        binding.tableIncidents.removeView(view)
                        binding.progressBar2.visibility = View.INVISIBLE
                    }
                    else {
                        when (response.code()){
                            401 -> {  // Unauthorized request or wrong token
                                Log.e("Request", "Unauthorized request")
                                if (authManager!!.isAccessTokenExpired(token)){
                                    Log.e("Token expiration", "Token expired")
                                    authManager!!.refreshToken(apiService)
                                }
                            }

                            400 -> { // ValidationProblem response
                                val errorBody = response.errorBody()?.string()
                                // No validation errors have appeared
                                Log.e("Error", errorBody.toString())
                                Log.e("State change", "Error spotted")

                                val apiResponse: APIResponse? = errorBody
                                    ?.let { utils.convertStringToObject<APIResponse?>(it) }

                                apiResponse?.errorMessages?.get(0)?.let {
                                    utils.showMessage("Incident status changes",
                                        it, requireContext()) }
                            }
                        }
                        binding.progressBar2.visibility = View.INVISIBLE
                    }
                }

                override fun onFailure(call: Call<APIResponse>, t: Throwable) {
                    // Handle failure here
                    Log.e("Api error",  t.message.toString())
                }
            })
        }
    }
}