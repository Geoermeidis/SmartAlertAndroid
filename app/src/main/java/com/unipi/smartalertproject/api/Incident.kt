package com.unipi.smartalertproject.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Incident(
    @SerializedName("categoryName") val categoryName: String,
    @SerializedName("comments") val comments: String,
    @SerializedName("submittedByUsername") val submittedByUsername: String,
    @SerializedName("photoURL") val photoUrl: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude")  val longitude: Double,
    @SerializedName("submittedAt") val submittedAt: String,
    @SerializedName("totalSubmissions") val totalSubmissions: Int,
    @SerializedName("state") val state: Int,
) : Parcelable