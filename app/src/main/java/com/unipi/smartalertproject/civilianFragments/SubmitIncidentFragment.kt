package com.unipi.smartalertproject.civilianFragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.drawToBitmap
import androidx.fragment.app.Fragment
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.unipi.smartalertproject.R
import com.unipi.smartalertproject.Utils
import com.unipi.smartalertproject.api.ApiService
import com.unipi.smartalertproject.api.AuthManager
import com.unipi.smartalertproject.api.RetrofitClient
import com.unipi.smartalertproject.databinding.FragmentSubmitIncidentBinding
import com.unipi.smartalertproject.helperFragments.CameraFragment


class SubmitIncidentFragment : Fragment(), CameraFragment.ISendDataFromDialog{
    private val apiService = RetrofitClient.retrofit.create(ApiService::class.java)

    private var _binding: FragmentSubmitIncidentBinding? = null
    private val binding get() = _binding!!
    private var authManager: AuthManager? = null
    private val utils: Utils = Utils()
    private lateinit var incidentImageName: String

    // Firebase storage
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val storageRef: StorageReference = storage.reference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSubmitIncidentBinding.inflate(inflater, container, false)
        authManager = AuthManager(requireContext())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonCamera.setOnClickListener{
            val camera = CameraFragment()
            camera.show(childFragmentManager, "CameraFragment")
        }

        binding.buttonSubmitIncident.setOnClickListener{
            
        }
    }

    private fun saveToFirebaseStorage(imageBytes: ByteArray, imageName: String){
        val imageRef = storageRef.child("images/${authManager?.getUserId()}/$imageName")

        val task: UploadTask = imageRef.putBytes(imageBytes)

        task.addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                val imageUrl: String = uri.toString()
                Log.d("Firebase storage", imageUrl)
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