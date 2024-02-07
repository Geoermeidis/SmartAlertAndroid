package com.unipi.smartalertproject.helperFragments

import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.unipi.smartalertproject.R
import com.unipi.smartalertproject.api.AuthManager
import com.unipi.smartalertproject.api.Incident
import com.unipi.smartalertproject.api.Utils
import com.unipi.smartalertproject.databinding.FragmentIncidentInfoDialogBinding
import com.unipi.smartalertproject.databinding.FragmentIncidentsPreviewBinding

class IncidentInfoDialogFragment : DialogFragment() {

    private lateinit var incident: Incident
    private var _binding: FragmentIncidentInfoDialogBinding? = null
    private val binding get() = _binding!!
    private val utils = Utils()

    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val storageRef: StorageReference = storage.reference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentIncidentInfoDialogBinding.inflate(inflater, container, false)

        val incident = arguments?.getParcelable("incident", Incident::class.java)
        if (incident != null){
            binding.categoryInfoText.text = incident.categoryName
            binding.commentsInfoText.text = incident.comments
            binding.dateInfoText.text = incident.submittedAt

            binding.submitsInfoText.text = incident.totalSubmissions.toString()

            val geocoder = Geocoder(requireContext())

            geocoder.getFromLocation(incident.latitude,incident.longitude,1) {
                addresses -> binding.locationInfoText.text = addresses[0].locality
            }

        }





        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonBacktoIncidents.setOnClickListener {
            dismiss()
        }
        setImageFromFirebaseStorage(incident.photoUrl)


    }

    private fun setImageFromFirebaseStorage(imagePath: String){
        val imageRef = storageRef.child(imagePath)

        imageRef.downloadUrl.addOnSuccessListener { uri ->
            Picasso.get().load(uri).into(binding.imageInfoText)
        }.addOnFailureListener {
            utils.showMessage("Image attached", "The image couldnt be loaded",
                requireContext())
        }
    }
}