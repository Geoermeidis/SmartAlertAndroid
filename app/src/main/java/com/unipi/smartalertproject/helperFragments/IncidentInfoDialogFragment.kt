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
import com.unipi.smartalertproject.R
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
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentIncidentInfoDialogBinding.inflate(inflater, container, false)

        incident = arguments?.getParcelable("incident", Incident::class.java)!!

        binding.categoryInfoText.text = incident.categoryName
        if (requireContext().resources.configuration.locales[0].toString() == "el_GR"){
            binding.categoryInfoText.text = resources.getStringArray(R.array.dangerCategoriesGr)
                .toList()[resources.getStringArray(R.array.dangerCategoriesEn).indexOf(incident.categoryName)]
        }

        binding.commentsInfoText.text = incident.comments
        binding.dateInfoText.text = incident.submittedAt

        binding.submitsInfoText.text = incident.totalSubmissions.toString()

        val geocoder = Geocoder(requireContext())

        geocoder.getFromLocation(incident.latitude,incident.longitude,1) {
            addresses -> binding.locationInfoText.text = addresses[0].locality
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val window = dialog?.window
        if (window != null) {
            val metrics = resources.displayMetrics
            val width = metrics.widthPixels * 0.9 // Change 0.8 to your desired proportion
            val height = metrics.heightPixels * 0.9 // Change 0.7 to your desired proportion
            window.setLayout(width.toInt(), height.toInt())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonBacktoIncidents.setOnClickListener {
            dismiss()
        }

        setImageFromFirebaseStorage(incident.photoUrl)
    }

    private fun setImageFromFirebaseStorage(imagePath: String){
        if (imagePath == "none"){
            val imageRef = storageRef.child(imagePath)

            imageRef.downloadUrl.addOnSuccessListener { uri ->
                Picasso.get().load(uri).into(binding.imageInfoText)
            }.addOnFailureListener {
                utils.showMessage(
                    getString(R.string.imageAttachedErrorHeader),
                    getString(R.string.imageAttachedErrorMessage),
                    requireContext())
            }
        }
    }
}