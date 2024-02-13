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
import android.widget.ArrayAdapter
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
import com.unipi.smartalertproject.databinding.FragmentIncidentStatsPreviewBinding
import com.unipi.smartalertproject.helperFragments.IncidentInfoDialogFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Date
import java.util.Locale
import kotlin.properties.Delegates

class IncidentStatsPreviewFragment : Fragment() {
    private val apiService = RetrofitClient.retrofit.create(ApiService::class.java)

    private var _binding: FragmentIncidentStatsPreviewBinding? = null
    private val binding get() = _binding!!
    private var authManager: AuthManager? = null

    // View model (in a way)
    private var incidents: List<Incident> = listOf()

    private var density by Delegates.notNull<Float>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment\
        _binding = FragmentIncidentStatsPreviewBinding.inflate(inflater, container, false)
        authManager = AuthManager(requireContext())

        density = resources.displayMetrics.density

        binding.tableIncidents.removeAllViews()
        addHeadersToTable()
        getIncidents()

        if (requireContext().resources.configuration.locales[0].toString() == "el_GR"){
            val adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.sort_categoriesGr,
                R.layout.custom_spinner
            )

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.pickerSortingClass.adapter = adapter
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonUpdateTable.setOnClickListener {
            binding.tableIncidents.removeAllViews()
            addHeadersToTable()
            incidents = emptyList()
            getIncidents()
        }

        binding.buttonOpenMapsView.setOnClickListener {
            val bundle = Bundle()
            bundle.putParcelableArray("incidents", incidents
                .filter { incident: Incident -> incident.state == 1 }
                .toTypedArray())
            findNavController().navigate(
                R.id.action_incidentStatsPreviewFragment_to_mapsFragment,
                bundle
            )
        }

        binding.pickerSortingClass.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedClass = resources.getStringArray(R.array.sort_categoriesEn)[position].lowercase()
                val selectedOrderIsAscending = binding.pickerSortingOrder.isSelected
                sortTable(selectedOrderIsAscending, selectedClass)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        binding.pickerSortingOrder.setOnCheckedChangeListener { _, selectedOrderIsAscending ->
            val selectedClass = resources
                .getStringArray(R.array.sort_categoriesEn)[binding.pickerSortingClass.selectedItemPosition]
                .lowercase()
            sortTable(selectedOrderIsAscending, selectedClass)
        }
    }

    private fun sortTable(selectedOrderIsAscending: Boolean, selectedClass: String){
        binding.tableIncidents.removeAllViews()
        addHeadersToTable()
        if (selectedClass == "all"){
            fillTable(incidents)
        }
        else if (selectedClass == "by date" && selectedOrderIsAscending){
            fillTable(incidents.sortedBy {Calendar.getInstance()
                .apply{
                    time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                                .parse(it.submittedAt)
                }
            })
        }
        else if (selectedClass == "by date"){
            fillTable(incidents.sortedByDescending { Calendar.getInstance()
                .apply{
                    time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                        .parse(it.submittedAt)
                }
            })
        }
        else{
            if (requireContext().resources.configuration.locales[0].toString() == "el_GR"){
                if (selectedClass == "by category" && selectedOrderIsAscending)
                    fillTable(incidents.sortedBy { resources.getStringArray(R.array.dangerCategoriesGr)
                        .toList()[resources.getStringArray(R.array.dangerCategoriesEn).indexOf(it.categoryName)]
                    })
                else if (selectedClass == "by category")
                    fillTable(incidents.sortedByDescending { resources.getStringArray(R.array.dangerCategoriesGr)
                        .toList()[resources.getStringArray(R.array.dangerCategoriesEn).indexOf(it.categoryName)]})
            }
            else {
                if (selectedClass == "by category" && selectedOrderIsAscending)
                    fillTable(incidents.sortedBy { it.categoryName })
                else if (selectedClass == "by category")
                    fillTable(incidents.sortedByDescending { it.categoryName })
            }
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

                        fillTable(incidents)

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

    private fun addHeadersToTable(){
        val padding = (2 * density + 0.5f).toInt()
        val categoryTextView = TextView(requireContext()).apply {
            text = getString(R.string.selectCategoryHeaderMessage)
            textAlignment =View.TEXT_ALIGNMENT_CENTER
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            setPadding(padding, padding, padding, padding)
            layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT, 1f)
        }

        val locationTextView = TextView(requireContext()).apply {
            text = getString(R.string.statsLocationLabel)
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            setPadding(padding, padding, padding, padding)
            layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT, 2f)
        }

        val dateTextView = TextView(requireContext()).apply {
            text = getString(R.string.statsDateActivationLabel)
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            setPadding(padding, padding, padding, padding)
            layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT, 1f)
        }

        val infoTextView = TextView(requireContext()).apply {
            text = "" // Assuming you want a space
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            setPadding(padding, padding, padding, padding)
            layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT, 1f)
        }

        binding.tableIncidents.apply {
            addView(TableRow(requireContext()).apply {
                addView(categoryTextView)
                addView(locationTextView)
                addView(dateTextView)
                addView(infoTextView)
            })
            addView(View(requireContext()).apply {
                layoutParams =  TableRow.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    (5 * density + 0.5f).toInt()) // Height in pixels
                setBackgroundColor(Color.GRAY)
            })
        }
    }

    private fun fillTable(incidentsList: List<Incident>){
        // get only accepted incidents
        incidentsList.filter { incident: Incident -> incident.state == 1 }
            .forEach { incident ->
                val padding = (2 * density + 0.5f).toInt()

                val categoryView = TextView(requireContext()).apply {

                    var catText = incident.categoryName
                    if (requireContext().resources.configuration.locales[0].toString() == "el_GR") {
                        catText = resources.getStringArray(R.array.dangerCategoriesGr)
                            .toList()[resources.getStringArray(R.array.dangerCategoriesEn)
                            .indexOf(incident.categoryName)]
                    }

                    catText = catText.replaceFirstChar { cat ->
                        if (cat.isLowerCase()) cat.titlecase(Locale.getDefault()) else cat.toString()
                    }

                    // TODO add cases for big screens or landscape?
                    text = catText

                    tooltipText = catText

                    maxWidth = (8 * density + 0.5f).toInt()
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
                    maxWidth = (12 * density + 0.5f).toInt()
                }

                val geocoder = Geocoder(requireContext())

                geocoder.getFromLocation(incident.latitude,incident.longitude,1) {
                    addresses ->
                        locationView.text = addresses[0].locality
                        locationView.tooltipText = "${addresses[0].countryName}, ${addresses[0].getAddressLine(0)}"
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
                            (2 * density + 0.5f).toInt(),
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

                val tableRow = TableRow(requireContext()).apply {
                    addView(categoryView)
                    addView(locationView)
                    addView(dateView)
                    addView(infoBtn)
                }

                binding.tableIncidents.addView(tableRow)
                binding.tableIncidents.addView(View(requireContext()).apply {
                    layoutParams =  TableRow.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        (5 * density + 0.5f).toInt()) // Height in pixels
                    setBackgroundColor(Color.GRAY)
                })
            }
    }
}