package com.unipi.smartalertproject.helperFragments


import android.app.Dialog
import android.location.Location
import android.location.Geocoder
import android.os.Bundle
import android.text.util.Linkify
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.DialogFragment
import com.google.firebase.Timestamp
import com.unipi.smartalertproject.R
import com.unipi.smartalertproject.api.Notification
import com.unipi.smartalertproject.api.categoryToDescription
import com.unipi.smartalertproject.api.categoryToInstruction
import com.unipi.smartalertproject.databinding.FragmentNotificationDialogBinding
import com.unipi.smartalertproject.services.LocationService
import com.unipi.smartalertproject.services.SoundService
import java.util.Calendar
import java.util.Date

class NotificationDialogFragment : DialogFragment() {

    private lateinit var incident: Notification
    private var _binding: FragmentNotificationDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawable(AppCompatResources
            .getDrawable(requireContext(), R.drawable.dialog_fragment_background)
        )

        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentNotificationDialogBinding.inflate(inflater, container, false)
        incident = arguments?.getParcelable("incident", Notification::class.java)!!

        val calendar: Calendar = Calendar.getInstance()
        val currentDate = calendar.time.toString()
        var submittedLocation = ""
        val geocoder = Geocoder(requireContext())

        geocoder.getFromLocation(incident.latitude,incident.longitude,1)
        { addresses ->
            submittedLocation = "${addresses[0].locality}, ${addresses[0].getAddressLine(0)}"

            binding.textNotificationContent.text = String.format(
                getString(R.string.notificationContentFormatted),
                currentDate,
                incident.categoryName,
                timestampToDateString(incident.submittedAt),
                submittedLocation,
                categoryToInstruction[incident.categoryName]?.let { getString(it) },
                categoryToDescription[incident.categoryName]?.let { getString(it) },
            )

            binding.textWebsite.text = String.format(getString(R.string.infoPromptNotification),
                incident.websiteURL)

            Linkify.addLinks(binding.textWebsite, Linkify.WEB_URLS);
        }

        return binding.root
    }

    private fun timestampToDateString(timestamp: Timestamp): String{
        val timestampInMillis = timestamp.seconds * 1000 + timestamp.nanoseconds / 1000000
        val date = Date(timestampInMillis)

        val calendar = Calendar.getInstance().apply {
            time = date
        }

        return String.format("%02d-%02d-%d %02d:%02d:%02d",
            calendar.get(Calendar.DAY_OF_MONTH),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            calendar.get(Calendar.SECOND)
        )
    }

    override fun onStart() {
        super.onStart()
        SoundService.getInstance().playSound(requireContext(), R.raw.alert_sound)
    }

    override fun onStop() {
        super.onStop()
        SoundService.getInstance().stopSound()
    }
}