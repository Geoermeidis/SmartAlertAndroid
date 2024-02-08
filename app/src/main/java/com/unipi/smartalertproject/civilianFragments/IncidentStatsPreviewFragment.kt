package com.unipi.smartalertproject.civilianFragments

import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TableRow
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.unipi.smartalertproject.R
import com.unipi.smartalertproject.api.ApiService
import com.unipi.smartalertproject.api.AuthManager
import com.unipi.smartalertproject.api.Incident
import com.unipi.smartalertproject.api.IncidentAPIResponse
import com.unipi.smartalertproject.api.RetrofitClient
import com.unipi.smartalertproject.api.Utils
import com.unipi.smartalertproject.databinding.FragmentIncidentStatsPreviewBinding
import com.unipi.smartalertproject.helperFragments.IncidentInfoDialogFragment
import kotlinx.coroutines.NonDisposableHandle.parent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Date
import java.util.Locale

class IncidentStatsPreviewFragment : Fragment() {
    private val apiService = RetrofitClient.retrofit.create(ApiService::class.java)

    private var _binding: FragmentIncidentStatsPreviewBinding? = null
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
        _binding = FragmentIncidentStatsPreviewBinding.inflate(inflater, container, false)
        authManager = AuthManager(requireContext())

        getIncidents()

        binding.buttonUpdateTable.setOnClickListener {
            binding.tableIncidents.removeViews(1, binding.tableIncidents.childCount-1)
            incidents = emptyList()
            getIncidents()
        }

        binding.buttonOpenMapsView.setOnClickListener{
//            val bundle = Bundle() // TODO implement map for users
            // TODO implement when in map and back to to call getIncidents()
//            bundle.putParcelableArray("incidents", incidents.toTypedArray())
//            findNavController().navigate(R.id.,
//                bundle)
        }

        binding.pickerSortingClass.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Handle item selection here
                binding.tableIncidents.removeViews(1, binding.tableIncidents.childCount-1)

                val selectedClass = parent?.getItemAtPosition(position).toString().lowercase()
                val selectedOrderIsAscending = binding.pickerSortingOrder.isSelected

                if (selectedClass == "all")
                    fillTable(incidents)
                else if (selectedClass == "by category" && selectedOrderIsAscending)
                    fillTable(incidents.sortedBy { it.categoryName })
                else if (selectedClass == "by category")
                    fillTable(incidents.sortedByDescending { it.categoryName })
                else if (selectedClass == "by date" && selectedOrderIsAscending)
                    fillTable(incidents.sortedBy { it.submittedAt })
                else
                    fillTable(incidents.sortedByDescending { it.submittedAt })
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        binding.pickerSortingOrder.setOnCheckedChangeListener { _, selectedOrderIsAscending ->
            binding.tableIncidents.removeViews(1, binding.tableIncidents.childCount-1)
            val selectedClass = binding.pickerSortingClass.selectedItem.toString().lowercase()

            if (selectedClass == "all")
                fillTable(incidents)
            else if (selectedClass == "by category" && selectedOrderIsAscending)
                fillTable(incidents.sortedBy { it.categoryName })
            else if (selectedClass == "by category")
                fillTable(incidents.sortedByDescending { it.categoryName })
            else if (selectedClass == "by date" && selectedOrderIsAscending)
                fillTable(incidents.sortedBy { it.submittedAt })
            else
                fillTable(incidents.sortedByDescending { it.submittedAt })
        }

        return binding.root
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
                        fillTable(incidents)
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

    private fun fillTable(incidentsList: List<Incident>){
        // get only accepted incidents
        incidentsList.filter { incident: Incident -> incident.state == 1 }
            .forEach { incident ->
                val density = resources.displayMetrics.density
                val padding = (2 * density + 0.5f).toInt()

                val categoryView = TextView(requireContext()).apply {
                    text = incident.categoryName.replaceFirstChar {cat ->
                        if (cat.isLowerCase()) cat.titlecase(Locale.getDefault()) else cat.toString()
                    }
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                    setPadding(padding, padding, padding, padding)
                    gravity = Gravity.CENTER
                }

                val locationView = TextView(requireContext()).apply {
                    text = incident.latitude.toString()
                    setPadding(padding, padding, padding, padding)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                    textAlignment = View.TEXT_ALIGNMENT_CENTER
                    gravity = Gravity.CENTER
                }

                val geocoder = Geocoder(requireContext())

                geocoder.getFromLocation(incident.latitude,incident.longitude,1) {
                    addresses ->
                        locationView.text = addresses[0].locality
                        locationView.tooltipText = "${addresses[0].countryName}, ${addresses[0].locality}, ${addresses[0].getAddressLine(0)}"
                }


                val dateView = TextView(requireContext()).apply {
                    val date: Calendar = Calendar.getInstance()
                    date.time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                        .parse(incident.submittedAt)

                    text = String.format("%d/%d/%d", date.get(Calendar.DAY_OF_MONTH),
                                        date.get(Calendar.MONTH) + 1, date.get(Calendar.YEAR))

                    setPadding(padding, padding, padding, padding)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                    textAlignment = View.TEXT_ALIGNMENT_CENTER
                    gravity = Gravity.CENTER
                    tooltipText = String.format("%02d-%02d-%d %02d:%02d:%02d",
                            date.get(Calendar.DAY_OF_MONTH),
                            date.get(Calendar.MONTH) + 1,
                            date.get(Calendar.YEAR),
                            date.get(Calendar.HOUR_OF_DAY),
                            date.get(Calendar.MINUTE),
                            date.get(Calendar.SECOND))
                }

                val infoBtn = ImageButton(requireContext()).apply {
                    setImageResource(R.drawable.info_icon)
                    scaleType = ImageView.ScaleType.FIT_XY
                    layoutParams = TableRow.LayoutParams((50 * density + 0.5f).toInt(),
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
                        infoDialog.show(childFragmentManager, "IncidentInfoDialogFragment")
                    }
                }

                val divider = View(requireContext()).apply {
                    layoutParams =  TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        (8 * density + 0.5f).toInt()) // Height in pixels
                    setBackgroundColor(Color.GRAY)
                }

                val tableRow = TableRow(requireContext()).apply {
                    addView(categoryView)
                    addView(locationView)
                    addView(dateView)
                    addView(infoBtn)
                }

                binding.tableIncidents.addView(tableRow)
                binding.tableIncidents.addView(divider)
            }
    }
}