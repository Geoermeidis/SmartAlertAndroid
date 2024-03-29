package com.unipi.smartalertproject.officerFragments

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
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.get
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
        val userScreenWidth = resources.displayMetrics.widthPixels / resources.displayMetrics.density

        val layoutParameters = TableRow.LayoutParams(0,0).apply {
            height = resources.getDimensionPixelSize(R.dimen.incident_button_height)
            width = resources.getDimensionPixelSize(R.dimen.incident_button_width)
            setMargins(
                resources.getDimensionPixelSize(R.dimen.stats_info_marginH),
                resources.getDimensionPixelSize(R.dimen.stats_info_marginV),
                resources.getDimensionPixelSize(R.dimen.stats_info_marginH),
                resources.getDimensionPixelSize(R.dimen.stats_info_marginV)
            )
        }

        // get only submitted incidents
        incidents.sortedByDescending { incident -> incident.totalSubmissions }
            .filter { incident: Incident -> incident.state == 0 }
            .forEach { incident ->
                val padding = resources.getDimensionPixelSize(R.dimen.stats_padding)
                val categoryView = TextView(requireContext()).apply {
                    var catText = incident.categoryName
                    if (requireContext().resources.configuration.locales[0].toString() == "el_GR") {
                        catText = resources.getStringArray(R.array.dangerCategoriesGr)
                            .toList()[resources.getStringArray(R.array.dangerCategoriesEn)
                            .indexOf(incident.categoryName)]
                    }
                    catText = catText.replaceFirstChar { cat ->
                        if (cat.isLowerCase()) cat.titlecase(Locale.getDefault())
                        else cat.toString()
                    }

                    text = catText
                    // maxWidth = (8 * density + 0.5f).toInt()
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.preview_content_size))
                    textAlignment = View.TEXT_ALIGNMENT_CENTER
                    gravity = Gravity.CENTER
                    layoutParams = TableRow.LayoutParams((binding.tableIncidents[0] as TableRow).getChildAt(0).width,
                        TableRow.LayoutParams.WRAP_CONTENT)
                }

                val submissionsView = TextView(requireContext()).apply {
                    text = incident.totalSubmissions.toString()
                    
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.preview_content_size))
                    textAlignment = View.TEXT_ALIGNMENT_CENTER
                    gravity = Gravity.CENTER
                    layoutParams = TableRow.LayoutParams(
                        (binding.tableIncidents[0] as TableRow).getChildAt(1).width,
                        TableRow.LayoutParams.WRAP_CONTENT)
                }

                val infoBtn = ImageButton(requireContext()).apply {
                    setImageResource(R.drawable.info_icon)
                    scaleType = ImageView.ScaleType.FIT_XY
                    layoutParams = layoutParameters
                    setPadding(padding, padding, padding, padding)
                    contentDescription =
                        context.getString(R.string.informationIncidentContentDescription)
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
                    layoutParams = layoutParameters
                    setPadding(padding, padding, padding, padding)
                    contentDescription = context.getString(R.string.rejectIncidentContentDescription)
                    setOnClickListener {
                        AlertDialog.Builder(requireContext()).apply {
                            setTitle(context.getString(R.string.officer_incident_reject_title))
                            setMessage(context.getString(R.string.officer_incident_reject_message))
                            setPositiveButton(context.getString(R.string.yes)) { dialog, _ ->
                                changeIncidentStatus(incident.id, "rejected", it.parent as View)
                                dialog.dismiss()
                            }
                            setNegativeButton(context.getString(R.string.no)) { dialog, _ ->
                                dialog.dismiss()
                            }.create().show()
                        }

                    }
                }

                val acceptBtn = ImageButton(requireContext()).apply {
                    setImageResource(R.drawable.accepted_icon)
                    scaleType = ImageView.ScaleType.FIT_XY
                    layoutParams = layoutParameters
                    setPadding(padding, padding, padding, padding)
                    contentDescription = context.getString(R.string.acceptIncidentContentDescription)
                    setOnClickListener {
                        AlertDialog.Builder(requireContext()).apply {
                            setTitle(context.getString(R.string.officer_incident_accept_title))
                            setMessage(context.getString(R.string.officer_incident_accept_message))
                            setPositiveButton(context.getString(R.string.yes)) { dialog, _ ->
                                changeIncidentStatus(incident.id, "accepted", it.parent as View)
                                dialog.dismiss()
                            }
                            setNegativeButton(context.getString(R.string.no)) { dialog, _ ->
                                dialog.dismiss()
                            }
                        }.create().show()

                    }
                }

                val divider = View(requireContext()).apply {
                    layoutParams =  TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        resources.getDimensionPixelSize(R.dimen.divider_size))
                    setBackgroundColor(Color.GRAY)
                }

                var tableRow: TableRow

                Log.i("Screen width", userScreenWidth.toString())
                if (userScreenWidth >= 600){ // tablet mode?
                    val geocoder = Geocoder(requireContext())

                    val locationView = TextView(requireContext()).apply {
                        text = incident.latitude.toString()
                        
                        setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.preview_content_size))
                        textAlignment = View.TEXT_ALIGNMENT_CENTER
                        gravity = Gravity.CENTER
                        layoutParams = TableRow.LayoutParams((binding.tableIncidents[0] as TableRow).getChildAt(3).width,
                            TableRow.LayoutParams.WRAP_CONTENT)
                    }

                    geocoder.getFromLocation(incident.latitude,incident.longitude,1) {
                            addresses ->
                        locationView.text = addresses[0].locality
                        locationView.tooltipText = "${addresses[0].countryName}, ${addresses[0].getAddressLine(0)}"
                    }

                    val dateView = TextView(requireContext()).apply {
                        val date: Calendar = Calendar.getInstance()
                        date.time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                            .parse(incident.submittedAt)
                        layoutParams = TableRow.LayoutParams((binding.tableIncidents[0] as TableRow).getChildAt(2).width,
                            TableRow.LayoutParams.WRAP_CONTENT)

                        text = String.format("%d/%d/%d", date.get(Calendar.DAY_OF_MONTH),
                            date.get(Calendar.MONTH) + 1, date.get(Calendar.YEAR))

                        
                        setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.preview_content_size))

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

                    tableRow = TableRow(requireContext()).apply {
                        addView(categoryView)
                        addView(submissionsView)
                        addView(dateView)
                        addView(locationView)
                        addView(infoBtn)
                        addView(acceptBtn)
                        addView(rejectBtn)
                    }
                }
                else{
                    tableRow = TableRow(requireContext()).apply {
                        addView(categoryView)
                        addView(submissionsView)
                        addView(infoBtn)
                        addView(acceptBtn)
                        addView(rejectBtn)
                    }
                }

                binding.tableIncidents.addView(tableRow)
                binding.tableIncidents.addView(divider)
        }
    }

    private fun getIncidents(){
        val token = authManager?.getAccessToken()
        if (token != null && authManager != null){
            binding.progressBarPreview.visibility = View.VISIBLE
            val call: Call<IncidentAPIResponse> = apiService.getIncidents("Bearer $token")
            // execute call and wait for response or fail
            call.enqueue(object : Callback<IncidentAPIResponse> {
                override fun onResponse(call: Call<IncidentAPIResponse>, response: Response<IncidentAPIResponse>) {
                    if (response.isSuccessful) {
                        val data = response.body()
                        incidents  = data?.result!!
                        fillTable()
                        binding.progressBarPreview.visibility = View.INVISIBLE
                        Log.e("Incidents", "Incidents acquired")
                    }
                    else {
                        // Access token has expired so we must refresh it
                        if (authManager!!.isAccessTokenExpired(token)){
                            Log.e("Token expiration", "Token expired")
                            authManager!!.refreshToken(apiService)
                            binding.progressBarPreview.visibility = View.INVISIBLE
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
        binding.progressBarPreview.visibility = View.VISIBLE
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
                        utils.showSuccessMessage(String.format(getString(R.string.incidentStateUpdateSuccess), state),
                            Toast.LENGTH_LONG, requireContext())
                        binding.tableIncidents.removeView(view)
                        binding.progressBarPreview.visibility = View.INVISIBLE
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
                        binding.progressBarPreview.visibility = View.INVISIBLE
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