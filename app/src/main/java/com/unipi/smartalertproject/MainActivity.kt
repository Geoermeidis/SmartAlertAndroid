package com.unipi.smartalertproject

import android.location.Location
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import androidx.core.os.bundleOf
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore
import com.unipi.smartalertproject.api.Notification
import com.unipi.smartalertproject.databinding.ActivityMainBinding
import com.unipi.smartalertproject.helperFragments.NotificationDialogFragment
import com.unipi.smartalertproject.services.LocationService

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val db = Firebase.firestore
    private var uses = 0
    private lateinit var locationService: LocationService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        locationService = LocationService(this)
        setSupportActionBar(binding.toolbar)

        baseContext.resources.configuration.locales[0]

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
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

                                Log.d("Item read", "Current data: ${notification}")
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

    override fun onStart() {
        super.onStart()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}