package com.unipi.smartalertproject.helperFragments

import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.unipi.smartalertproject.R
import com.unipi.smartalertproject.api.Notification
import com.unipi.smartalertproject.api.categoryToDescription
import com.unipi.smartalertproject.api.categoryToInstruction
import com.unipi.smartalertproject.databinding.FragmentNotificationDialogBinding
import java.util.Calendar

class NotificationDialogFragment : DialogFragment() {

    private lateinit var incident: Notification
    private var _binding: FragmentNotificationDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

         _binding = FragmentNotificationDialogBinding.inflate(inflater, container, false)
        incident = savedInstanceState?.getParcelable("incident", Notification::class.java)!!

        //        [MaxDistanceSubmission] = 30
        //        [MaxDistanceNotification] = 50
        //        [MaxTimeForNewIncident] = 120
        //        [TimeForNotification] = 400

        val calendar: Calendar = Calendar.getInstance()
        val currentDate = calendar.time.toString()
        var location = ""
        val geocoder = Geocoder(requireContext())

        geocoder.getFromLocation(incident.latitude,incident.longitude,1) {
                addresses ->
            location = "${addresses[0].locality}, ${addresses[0].getAddressLine(0)}"
        }

        binding.textNotificationContent.text = String.format(
            getString(R.string.notificationContentFormatted),
            currentDate,
            incident.categoryName,
            incident.submittedAt,
            location ,
            categoryToInstruction[incident.categoryName],
            categoryToDescription[incident.categoryName]
        )

        binding.textWebsite.text = String.format(getString(R.string.infoPromptNotification),
            incident.websiteURL)

        return binding.root
    }
}