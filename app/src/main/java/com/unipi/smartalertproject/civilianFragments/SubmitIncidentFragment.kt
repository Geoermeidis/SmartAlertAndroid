package com.unipi.smartalertproject.civilianFragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.drawToBitmap
import androidx.core.view.isVisible
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
import com.unipi.smartalertproject.api.Models.ValidationProblem
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
    private lateinit var imageBytes: ByteArray

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

        var longitude: Double
        var latitude: Double

        locationService.getLocation { location ->
            binding.progressBar.visibility = View.VISIBLE
            // Handle the obtained location here
            latitude = location["latitude"] ?: -181.0
            longitude = location["longitude"] ?: -181.0

            val token = authManager?.getAccessToken()
            val userId = authManager!!.getUserId() // check for user id nullable first
            if (latitude != -181.0 && authManager != null && token != null && userId != null){
                // Get incident data
                val comments = binding.textComments.text.toString()
                val photoUrl = "$folder$incidentImageName"

                // create incident
                val incidentData = Incident( userId, longitude, latitude, comments, photoUrl, category)

                // create call and send authorization token
                val call: Call<APIResponse> = apiService.submitIncident("Bearer $token",
                    incidentData)

                // execute call and wait for response or fail
                call.enqueue(object : Callback<APIResponse> {

                    override fun onResponse(call: Call<APIResponse>, response: Response<APIResponse>) {
                        Log.i("Call", "Call responded")
                        if (response.isSuccessful) { // Handle successful response here
                            val data: APIResponse? = response.body()

                            if (incidentImageName.isNotEmpty() && imageBytes.isNotEmpty())
                            {
                                Log.e("Image name", incidentImageName)
                                Log.e("Image bytes", imageBytes.toString())
                                saveToFirebaseStorage(imageBytes, incidentImageName)
                            }

                            binding.progressBar.visibility = View.INVISIBLE

                            utils.showSuccessMessage("You have submitted your incident",
                                Toast.LENGTH_LONG, requireContext())
                        }
                        else {
                            when (response.code()){
                                401 -> {  // Unauthorized request or wrong token
                                    Log.e("Request", "Unauthorized request")
                                    binding.progressBar.visibility = View.INVISIBLE
                                    if (authManager!!.isAccessTokenExpired(token)){
                                        Log.e("Token expiration", "Token expired")
                                        authManager!!.refreshToken(apiService)
                                    }
                                }

                                400 -> { // ValidationProblem response
                                    val errorBody = response.errorBody()?.string()
                                    val validationProblem: ValidationProblem? = errorBody?.let {
                                        utils.convertStringToObject<ValidationProblem>(it)
                                    }

                                    if (validationProblem?.type != null) {
                                        // Handle validation problem
                                        Log.e("Submission error", "Validation error spotted")
                                        Log.e("Submission error", utils.mapToString(validationProblem.errors))
                                        utils.showScrollableDialog(requireContext(), "Fields validation",
                                            utils.mapToString(validationProblem.errors))
                                    }
                                    else {
                                        // No validation errors have appeared
                                        Log.e("Submission process", "Error spotted")

                                        val apiResponse: APIResponse? = errorBody
                                            ?.let { utils.convertStringToObject<APIResponse?>(it) }

                                        apiResponse?.errorMessages?.get(0)?.let {
                                            utils.showMessage("Incident submission",
                                                it, requireContext()) }

                                    }
                                }
                            }

                        }
                    }

                    override fun onFailure(call: Call<APIResponse>, t: Throwable) {
                        // Handle failure here
                        binding.progressBar.visibility = View.INVISIBLE
                        Log.e("Api error",  t.message.toString())
                    }
                })
            }
        }


    }

    override fun sendByteArray(array: ByteArray) {
        val bmp = BitmapFactory.decodeByteArray(array, 0, array.size)
        imageBytes = array
        val image = binding.viewPhoto
        image.setImageBitmap(Bitmap.createScaledBitmap(bmp, image.width, image.height, false))
    }

    override fun sendImageName(name: String) {
        incidentImageName = name
    }

}