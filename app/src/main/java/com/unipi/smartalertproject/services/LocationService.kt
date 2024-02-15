package com.unipi.smartalertproject.services

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.unipi.smartalertproject.MainActivity
import com.unipi.smartalertproject.R
import com.unipi.smartalertproject.api.Utils

// TODO test noise notification

class LocationService() {
    private lateinit var _fragment: Fragment
    private lateinit var _activity: MainActivity
    private lateinit var context: Context
    private lateinit var locationPermissionRequest: ActivityResultLauncher<Array<String>>
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private val utils: Utils = Utils()
    private var latitude: Double = -181.0
    private var longitude: Double = -181.0

    constructor(activity: MainActivity): this(){
        _activity = activity
        context = _activity.baseContext
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        locationPermissionRequest = _activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
                permissions -> // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && !it.value)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                utils.showMessage(context.getString(R.string.permissionsCameraHeader),
                    context.getString(R.string.permissionRequestDeniedMessage),
                    context)
            }
            else {
                Log.i("Permissions", "Permissions granted")
            }
        }
    }

    constructor(fragment: Fragment): this(){
        _fragment = fragment
        context = _fragment.requireContext()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        locationPermissionRequest = _fragment.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
                permissions -> // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && !it.value)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                utils.showMessage(context.getString(R.string.permissionsCameraHeader),
                    context.getString(R.string.permissionRequestDeniedMessage),
                    context)
            }
            else {
                Log.i("Permissions", "Permissions granted")
            }
        }
    }

    fun getLocation(callback: (Map<String, Double>) -> Unit) {
        requestUpdatesForLocation(callback)
    }

    private fun requestUpdatesForLocation(callback: (Map<String, Double>) -> Unit){
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {  // Permissions where not granted so we have to ask again
            locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,1000)
                .apply {
                    setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
                    setWaitForAccurateLocation(true)
                }.build()

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    if (locationResult.lastLocation != null) {
                        val location = locationResult.lastLocation
                        if (location != null) {
                            longitude = location.longitude
                            latitude = location.latitude
                        }

                        callback(mapOf("latitude" to latitude, "longitude" to longitude))

                        // Remove location updates after getting the first accurate location
                        fusedLocationClient.removeLocationUpdates(locationCallback)
                    }
                }
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
        else{
            requestPermissions()
        }
    }

    private fun requestPermissions() {
        locationPermissionRequest.launch(REQUIRED_PERMISSIONS)
    }

    companion object {
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ).apply {
            }.toTypedArray()
    }
}