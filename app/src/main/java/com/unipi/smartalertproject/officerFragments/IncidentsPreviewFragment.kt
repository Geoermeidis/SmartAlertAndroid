package com.unipi.smartalertproject.officerFragments

import android.app.ActionBar.LayoutParams
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.text.Layout.Alignment
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TableRow
import android.widget.TextView
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.unipi.smartalertproject.R
import com.unipi.smartalertproject.api.APIResponse
import com.unipi.smartalertproject.api.ApiService
import com.unipi.smartalertproject.api.AuthManager
import com.unipi.smartalertproject.api.Incident
import com.unipi.smartalertproject.api.IncidentAPIResponse
import com.unipi.smartalertproject.api.RetrofitClient
import com.unipi.smartalertproject.api.Utils
import com.unipi.smartalertproject.databinding.FragmentIncidentsPreviewBinding
import com.unipi.smartalertproject.databinding.FragmentSubmitIncidentBinding
import com.unipi.smartalertproject.helperFragments.LocationService
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
    ): View? {
        // Inflate the layout for this fragment\
        _binding = FragmentIncidentsPreviewBinding.inflate(inflater, container, false)
        authManager = AuthManager(requireContext())

        getIncidents()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun fillTable(){
        val geocoder = Geocoder(requireContext())

        incidents.forEach { incident ->
            var addressCity= "Birmingham"
            var address = ""
            val density = resources.displayMetrics.density
//            geocoder.getFromLocation(incident.latitude,incident.longitude,1,
//                object : Geocoder.GeocodeListener{
//                    override fun onGeocode(addresses: MutableList<Address>) {
//                        addressCity = addresses[0].locality
//                        address = addresses[0].toString()
//                        // code
//                    }
//                }
//            )

            val categoryView = TextView(requireContext()).apply {
                text = incident.categoryName.replaceFirstChar {cat ->
                    if (cat.isLowerCase()) cat.titlecase(Locale.getDefault()) else cat.toString()
                }
                setPadding(3, 3, 3, 3)
            }

            val submissionsView = TextView(requireContext()).apply {
                text = incident.totalSubmissions.toString()
                setPadding(3, 3, 3, 3)
                textAlignment = View.TEXT_ALIGNMENT_CENTER
            }

            val infoBtn = Button(requireContext()).apply {
                setBackgroundColor(Color.BLUE)
                layoutParams = TableRow.LayoutParams((40 * density + 0.5f).toInt(),
                    (40 * density + 0.5f).toInt()).apply {
                    setMargins(
                        (10 * density + 0.5f).toInt(),
                        (3 * density + 0.5f).toInt(),
                        (2 * density + 0.5f).toInt(),
                        (3 * density + 0.5f).toInt()
                    )
                }
                setPadding(3, 3, 3, 3)
                setOnClickListener {

                }
            }

            val photoBtn = Button(requireContext()).apply {
                setBackgroundColor(Color.GRAY)
                layoutParams = TableRow.LayoutParams((40 * density + 0.5f).toInt(),
                    (40 * density + 0.5f).toInt()).apply {
                    setMargins(
                        (2 * density + 0.5f).toInt(),
                        (3 * density + 0.5f).toInt(),
                        (2 * density + 0.5f).toInt(),
                        (3 * density + 0.5f).toInt()
                    )
                }
                setPadding(3, 3, 3, 3)
                setOnClickListener {

                }
            }

            val rejectBtn = Button(requireContext()).apply {
                setBackgroundColor(Color.RED)
                layoutParams = TableRow.LayoutParams((40 * density + 0.5f).toInt(),
                    (40 * density + 0.5f).toInt()).apply {
                    setMargins(
                        (2 * density + 0.5f).toInt(),
                        (3 * density + 0.5f).toInt(),
                        (2 * density + 0.5f).toInt(),
                        (3 * density + 0.5f).toInt()
                    )
                }
                setPadding(3, 3, 3, 3)
                setOnClickListener {

                }
            }

            val acceptBtn = Button(requireContext()).apply {
                setBackgroundColor(Color.GREEN)
                layoutParams = TableRow.LayoutParams((40 * density + 0.5f).toInt(),
                    (40 * density + 0.5f).toInt()).apply {
                    setMargins(
                        (2 * density + 0.5f).toInt(),
                        (3 * density + 0.5f).toInt(),
                        (2 * density + 0.5f).toInt(),
                        (3 * density + 0.5f).toInt()
                    )
                }
                setPadding(3, 3, 3, 3)
                setOnClickListener {

                }
            }

            val divider = View(requireContext()).apply {
                layoutParams =  TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 10) // Height in pixels
                setBackgroundColor(Color.GRAY)
            }

            val tableRow = TableRow(requireContext()).apply {
                addView(categoryView)
                addView(submissionsView)
                addView(infoBtn)
                addView(photoBtn)
                addView(acceptBtn)
                addView(rejectBtn)
            }


            // todo add divider view in rows and columns
            // todo make date appear only the date and in hover appear the time too
            // todo fix geocoder

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


}