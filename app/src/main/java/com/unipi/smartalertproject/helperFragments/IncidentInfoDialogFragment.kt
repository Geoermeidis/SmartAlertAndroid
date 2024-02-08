package com.unipi.smartalertproject.helperFragments

import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import com.unipi.smartalertproject.api.Incident
import com.unipi.smartalertproject.api.Utils
import com.unipi.smartalertproject.databinding.FragmentIncidentInfoDialogBinding

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

        incident = arguments?.getParcelable("incident", Incident::class.java)!!

        binding.categoryInfoText.text = incident.categoryName
        binding.commentsInfoText.text = incident.comments
        binding.dateInfoText.text = incident.submittedAt

        binding.submitsInfoText.text = incident.totalSubmissions.toString()

        val geocoder = Geocoder(requireContext())

        geocoder.getFromLocation(incident.latitude,incident.longitude,1) {
            addresses -> binding.locationInfoText.text = addresses[0].locality
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonBacktoIncidents.setOnClickListener {
            dismiss()
        }

        // TODO if photo is none while user is submitting then update the none photo
        //  with the one the latest user submitted

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