package com.unipi.smartalertproject.civilianFragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.drawToBitmap
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.unipi.smartalertproject.R
import com.unipi.smartalertproject.Utils
import com.unipi.smartalertproject.api.ApiService
import com.unipi.smartalertproject.api.AuthManager
import com.unipi.smartalertproject.api.Models.APIResponse
import com.unipi.smartalertproject.api.Models.Incident
import com.unipi.smartalertproject.api.Models.LoginInfo
import com.unipi.smartalertproject.api.RetrofitClient
import com.unipi.smartalertproject.databinding.FragmentSubmitIncidentBinding
import com.unipi.smartalertproject.helperFragments.CameraFragment
import com.unipi.smartalertproject.helperFragments.LocationService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SubmitIncidentFragment : Fragment(), CameraFragment.ISendDataFromDialog{
    private val apiService = RetrofitClient.retrofit.create(ApiService::class.java)

    private var _binding: FragmentSubmitIncidentBinding? = null
    private val binding get() = _binding!!
    private var authManager: AuthManager? = null
    private val utils: Utils = Utils()

    // Camera
    private lateinit var incidentImageName: String

    // Location
    private lateinit var locationService: LocationService

    // Firebase storage
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val storageRef: StorageReference = storage.reference
    private lateinit var folder: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSubmitIncidentBinding.inflate(inflater, container, false)
        authManager = AuthManager(requireContext())
        folder = "images/${authManager?.getUserId()}/"
        incidentImageName = ""
        locationService = LocationService(this)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonCamera.setOnClickListener{
            val camera = CameraFragment()
            camera.show(childFragmentManager, "CameraFragment")
        }

        binding.buttonSubmitIncident.setOnClickListener{
            submitIncident()
        }
    }

    private fun saveToFirebaseStorage(imageBytes: ByteArray, imageName: String){
        val imageRef = storageRef.child("$folder$imageName")

        val task: UploadTask = imageRef.putBytes(imageBytes)

        task.addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                val imageUrl: String = uri.toString()
                Log.d("Firebase storage", imageUrl)
            }
        }

    }

    private fun submitIncident(){
        val category = binding.spinner.selectedItem.toString()
        Log.i("Checking category", category)
        if (category.isEmpty()){
            utils.showMessage("Category", "Select category before submitting", requireContext())
            return
        }

        var longitude: Double = -1.0
        var latitude: Double = -1.0

        locationService.getLocation { location ->
            // Handle the obtained location here
            latitude = location["latitude"] ?: -1.0
            longitude = location["longitude"] ?: -1.0

            val token = authManager?.getAccessToken()
            val userId = authManager!!.getUserId()
            if (latitude != -1.0 && authManager != null && token != null && userId != null){
                // Get incident data

                val comments = binding.textComments.text.toString()
                val photoUrl = "$folder$incidentImageName"

                // check for user id nullable first
                val incidentData = Incident( userId, longitude, latitude, comments, photoUrl, category)

                // create call and send authorization token
                val call: Call<APIResponse> = apiService.submitIncident("Bearer $token",  incidentData)
                //getIncidents("Bearer $token")
                // execute call and wait for response or fail
                call.enqueue(object : Callback<APIResponse> {

                    override fun onResponse(call: Call<APIResponse>, response: Response<APIResponse>) {
                        Log.i("Call", "Call responded")
                        if (response.isSuccessful) { // Handle successful response here
                            val data: APIResponse? = response.body()
                            utils.showSuccessMessage("You have a submitted your incident",
                                Toast.LENGTH_LONG, requireContext())
                        }
                        else {
                            // Get api error messages
                            Log.e("Response code", response.code().toString())
                            Log.e("Error message", response.message())
                            if (authManager!!.isAccessTokenExpired(token)){
                                Log.e("Token expiration", "Token expired")
                                authManager!!.refreshToken(apiService)
                                // submitIncident()
                            }
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

    override fun sendByteArray(array: ByteArray) {
        val bmp = BitmapFactory.decodeByteArray(array, 0, array.size)
        val image = binding.viewPhoto
        image.setImageBitmap(Bitmap.createScaledBitmap(bmp, image.width, image.height, false))
    }

    override fun sendImageName(name: String) {
        incidentImageName = name
    }

}