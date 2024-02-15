package com.unipi.smartalertproject.helperFragments

import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.AdvancedMarkerOptions
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PinConfig
import com.unipi.smartalertproject.R
import com.unipi.smartalertproject.api.ApiService
import com.unipi.smartalertproject.api.AuthManager
import com.unipi.smartalertproject.api.Incident
import com.unipi.smartalertproject.api.IncidentAPIResponse
import com.unipi.smartalertproject.api.RetrofitClient
import okhttp3.internal.wait
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class MapsFragment : Fragment() {

    private lateinit var incidents: List<Incident>
    private var authManager: AuthManager? = null
    private val apiService = RetrofitClient.retrofit.create(ApiService::class.java)
    // TODO
//    private val disasterToColor = mapOf(
//        "tornado"    to ResourcesCompat.getColor(resources, R.color.tornado, null),
//        "earthquake" to ResourcesCompat.getColor(resources, R.color.earthquake, null),
//        "avalanche"  to ResourcesCompat.getColor(resources, R.color.avalanche, null),
//        "landslide"  to ResourcesCompat.getColor(resources, R.color.landslide, null),
//        "blizzard"   to ResourcesCompat.getColor(resources, R.color.blizzard, null),
//        "storm"      to ResourcesCompat.getColor(resources, R.color.storm, null),
//        "tsunami"    to ResourcesCompat.getColor(resources, R.color.tsunami, null),
//        "floods"     to ResourcesCompat.getColor(resources, R.color.floods, null),
//        "cyclone"    to ResourcesCompat.getColor(resources, R.color.cyclone, null),
//        "wildfire"   to ResourcesCompat.getColor(resources, R.color.wildfire, null),
//        "urban fire" to ResourcesCompat.getColor(resources, R.color.urbanfire, null),
//        "volcano"    to ResourcesCompat.getColor(resources, R.color.volcano, null),
//        "heatwave"   to ResourcesCompat.getColor(resources, R.color.heatwave, null)
//    )

    private val callback = OnMapReadyCallback { googleMap ->
        Log.i("On callback", "")
        // get only submitted for officer and accepted for user
        // get only events for the last week for the civilians so the map does not get crowded
        getIncidents(googleMap)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        try{
            incidents = arguments?.getParcelableArray("incidents", Incident::class.java)!!.toList()
        }
        catch (_: NullPointerException){

        }

        authManager = AuthManager(requireContext())

        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    private fun getIncidents(googleMap: GoogleMap){
        val token = authManager?.getAccessToken()
        if (token != null && authManager != null){
            val call: Call<IncidentAPIResponse> = apiService.getIncidents("Bearer $token")
            // execute call and wait for response or fail
            call.enqueue(object : Callback<IncidentAPIResponse> {
                override fun onResponse(call: Call<IncidentAPIResponse>, response: Response<IncidentAPIResponse>) {
                    if (response.isSuccessful) {
                        val data = response.body()
                        incidents  = data?.result!!
                        val incidentsList: List<Incident> =
                            if (authManager?.getUserRole() == "Civilian")
                                incidents.filter {it.state == 1 }
                                    .filter {
                                        Calendar.getInstance().apply{
                                            time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                                                .parse(it.submittedAt)
                                        }
                                            .time >=
                                                Calendar.getInstance().apply {
                                                    add(Calendar.DAY_OF_MONTH, -30)}.time
                                    }
                            else
                                incidents.filter { it.state == 0 }

                        incidentsList.forEach {incident ->
                            // val category: String = disasterToColor.keys.toList()[(0 until disasterToColor.keys.toList().size).random()]
                            val location = LatLng(incident.latitude, incident.longitude)

                            val markerDefault = MarkerOptions()
                                .position(location)
                                .title(
                                    "${getString(R.string.mapMarkerMessageSubmission1)}${incident.submittedAt}\n${getString(R.string.mapsMarkerMessageSubmission2)}${incident.totalSubmissions}"
                                )

                            val marker = AdvancedMarkerOptions()
                            marker.position(location)
                                .title(
                                    "${getString(R.string.mapMarkerMessageSubmission1)}${incident.submittedAt}\n${getString(R.string.mapsMarkerMessageSubmission2)}${incident.totalSubmissions}"
                                )

                            if (googleMap.mapCapabilities.isAdvancedMarkersAvailable){
                                val pinConfigBuilder: PinConfig.Builder = PinConfig.builder().apply {
                                    setBackgroundColor(Color.YELLOW)
                                    setBorderColor(Color.BLUE)
                                }
                                // disasterToColor[category]?.let {
                                // pinConfigBuilder.setBackgroundColor(it)
                                //}
                                val pinConfig: PinConfig = pinConfigBuilder.build()

                                marker.icon(BitmapDescriptorFactory.fromPinConfig(pinConfig))
                                googleMap.addMarker(marker)
                            }
                            else
                                googleMap.addMarker(markerDefault)
                        }

                        val athens = LatLng(37.983810, 23.727539)
                        googleMap.moveCamera(CameraUpdateFactory.newLatLng(athens))
                        googleMap.uiSettings.apply{
                            isMyLocationButtonEnabled = true
                            isCompassEnabled = true
                        }
                        Log.e("Incidents", "Incidents acquired")
                    }
                    else {
                        // Access token has expired so we must refresh it
                        if (authManager!!.isAccessTokenExpired(token)){
                            Log.e("Token expiration", "Token expired")
                            authManager!!.refreshToken(apiService)
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