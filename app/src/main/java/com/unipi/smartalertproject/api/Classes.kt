package com.unipi.smartalertproject.api

import com.google.gson.annotations.SerializedName
import java.time.OffsetDateTime
import java.util.Date

data class APIResponse(
    @SerializedName("result") val result: Any?,
    @SerializedName("errorMessages") val errorMessages:List<String>
)

data class IncidentAPIResponse(
    @SerializedName("result") val result: List<Incident>,
    @SerializedName("errorMessages") val errorMessages:List<String>
)


class LoginInfo(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

data class Tokens(
   @SerializedName("accessToken") val accessToken: String,
   @SerializedName("refreshToken") val refreshToken: String
)

data class RegisterInfo(
    @SerializedName("username") val username: String,
    @SerializedName("firstname") val firstname: String,
    @SerializedName("lastname") val lastname: String,
    @SerializedName("phoneNumber") val phoneNumber: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class ValidationProblem(
    @SerializedName("type") val type: String,
    @SerializedName("title") val title: String,
    @SerializedName("status") val status: Int,
    @SerializedName("errors") val errors: Map<String, List<String>>
)

data class CreateIncidentDTO(
    @SerializedName("userId") val userId: String,
    @SerializedName("longitude")  val longitude: Double,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("comments") val comments: String,
    @SerializedName("photoURL") val photoUrl: String = "",
    @SerializedName("categoryName") val categoryName: String
)

