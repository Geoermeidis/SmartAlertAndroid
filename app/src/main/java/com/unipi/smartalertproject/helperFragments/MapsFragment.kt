package com.unipi.smartalertproject.helperFragments

import android.graphics.Color
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.AdvancedMarkerOptions
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PinConfig
import com.unipi.smartalertproject.R
import com.unipi.smartalertproject.api.Incident

class MapsFragment : Fragment() {

    private lateinit var incidents: Array<Incident>
    // TODO when returning to the incidents preview fragment incidents must show
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
        incidents.forEach {incident ->
            // val category: String = disasterToColor.keys.toList()[(0 until disasterToColor.keys.toList().size).random()]
            val location = LatLng(incident.latitude, incident.longitude)

            val markerDefault = MarkerOptions()
                .position(location)
                .title("First submitted at ${incident.submittedAt}\n" +
                        "Total submissions: ${incident.totalSubmissions}")

            val marker = AdvancedMarkerOptions()
            marker.position(location)
                .title("First submitted at ${incident.submittedAt}\n" +
                    "Total submissions: ${incident.totalSubmissions}")

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
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.i("On create view", "")
        incidents = arguments?.getParcelableArray("incidents", Incident::class.java)!!
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }
}