package com.unipi.smartalertproject

import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore
import com.unipi.smartalertproject.api.ApiService
import com.unipi.smartalertproject.api.AuthManager
import com.unipi.smartalertproject.api.Notification
import com.unipi.smartalertproject.api.RetrofitClient
import com.unipi.smartalertproject.databinding.ActivityMainBinding
import com.unipi.smartalertproject.helperFragments.NotificationDialogFragment
import com.unipi.smartalertproject.services.LocationService

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var apiService: ApiService
    private lateinit var authManager: AuthManager
    private val db = Firebase.firestore
    private var uses = 0
    private lateinit var locationService: LocationService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        authManager = AuthManager(baseContext)
        apiService = RetrofitClient.retrofit.create(ApiService::class.java)
        locationService = LocationService(this)

        // Refresh token every time the user logs in
        // That happens only if the refresh token is valid

        // If refresh token is expired or user has not logged in yet then go to login page
        if (authManager.getUserId() == null || authManager.isRefreshTokenExpired()){
            Navigation.findNavController(findViewById(R.id.nav_host_fragment_content_main)).navigate(R.id.FirstFragment)
        }
        else{
            // refresh user access token
            authManager.refreshToken(apiService)
            if (!authManager.getUserRole().isNullOrEmpty()){
                if ( authManager.getUserRole() == "Officer"){
                    Navigation.findNavController(findViewById(R.id.nav_host_fragment_content_main)).navigate(R.id.mainMenuOfficerFragment)
                }
                else{
                    Navigation.findNavController(findViewById(R.id.nav_host_fragment_content_main)).navigate(R.id.mainMenuCivilianFragment)
                }
            }

        }

        val docRef = db.collection("Incidents")

        docRef.addSnapshotListener { snapshot, e ->

            if (e != null) {
                Log.w("Item read", "Listen failed.", e)
                return@addSnapshotListener
            }

            Log.i("Information uses", uses.toString())
            if (snapshot != null && !snapshot.isEmpty) {
                val data = snapshot.documents.last().data

                if (data != null && uses > 0){
                    val notification = Notification(
                        categoryName = data["CategoryName"].toString(),
                        submittedAt = data["SubmittedAt"] as Timestamp,
                        latitude = data["Latitude"] as Double,
                        longitude = data["Longitude"] as Double,
                        maxDistanceNotification = data["MaxDistanceNotification"] as Long,
                        websiteURL = data["WebsiteURL"].toString()
                    )

                    // Get current location
                    locationService.getLocation { currentLocation ->

                        val currentLatitude = currentLocation["latitude"]
                        val currentLongitude = currentLocation["longitude"]
                        val results = FloatArray(3)

                        // check for nullables
                        if (currentLatitude != null && currentLongitude != null) {
                            // get results from comparison of the 2 locations

                            Log.i("Current location", "$currentLatitude, $currentLongitude")
                            Log.i(
                                "Incident location",
                                "${notification.latitude}, ${notification.longitude}"
                            )
                            Log.i("Max distance in meters", "${notification.maxDistanceNotification}")

                            Location.distanceBetween(
                                notification.latitude, notification.longitude,
                                currentLatitude, currentLongitude, results
                            )

                            Log.i("Distance difference", "${results[0]}")
                            if ( results[0] <= 1.1 * notification.maxDistanceNotification){
                                val bundle = bundleOf("incident" to notification)
                                val notificationDialog = NotificationDialogFragment()
                                notificationDialog.arguments = bundle

                                notificationDialog.show(this.supportFragmentManager,
                                    "NotificationDialogFragment")

                                Log.d("Item read", "Current data: $notification")
                            }
                        }
                    }

                }

                uses += 1

            } else {
                Log.d("Item read", "Current data: null")
            }
        }

    }
}