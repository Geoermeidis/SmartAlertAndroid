package com.unipi.smartalertproject

import android.os.Bundle
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
import androidx.fragment.app.FragmentActivity
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.unipi.smartalertproject.api.Notification
import com.unipi.smartalertproject.databinding.ActivityMainBinding
import com.unipi.smartalertproject.helperFragments.IncidentInfoDialogFragment
import com.unipi.smartalertproject.helperFragments.NotificationDialogFragment

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

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

            if (snapshot != null && !snapshot.isEmpty) {
                val data = snapshot.documents.last().data
                if (data != null){
                    val notification = Notification(
                        categoryName = data["CategoryName"].toString(),
                        submittedAt = data["SubmittedAt"].toString(),
                        latitude = data["Latitude"] as Double,
                        longitude = data["Longitude"] as Double,
                        maxDistanceNotification = data["MaxDistanceNotification"] as Long,
                        websiteURL = data["WebsiteURL"].toString()
                    )

                    // TODO test
                    // TODO check if user is in range based on max distance

                    val bundle = bundleOf("incident" to notification)
                    val notificationDialog = NotificationDialogFragment()
                    notificationDialog.arguments = bundle

                    notificationDialog.show(this.supportFragmentManager,
                        "NotificationDialogFragment")

                    Log.d("Item read", "Current data: ${notification.websiteURL}")
                }

            } else {
                Log.d("Item read", "Current data: null")
            }
        }

        Log.d("Main activity", "Main activity is here bitches")

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