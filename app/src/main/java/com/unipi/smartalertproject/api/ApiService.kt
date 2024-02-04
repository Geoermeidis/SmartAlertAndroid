package com.unipi.smartalertproject.api

import com.unipi.smartalertproject.api.Models.APIResponse
import com.unipi.smartalertproject.api.Models.Incident
import com.unipi.smartalertproject.api.Models.LoginInfo
import com.unipi.smartalertproject.api.Models.RegisterInfo
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @POST("api/login")
    fun loginUser(@Body loginInfo: LoginInfo): Call<APIResponse>
    @POST("api/register")
    fun registerUser(@Body registerInfo: RegisterInfo): Call<APIResponse>
    @POST("api/refresh-token")
    fun refreshToken(@Body refreshToken: String): Call<APIResponse>
    @GET("incidents")
    fun getIncidents(@Header("Authorization") accessToken: String): Call<APIResponse>
    @POST("incidents/create")
    fun submitIncident(@Header("Authorization") accessToken: String, @Body incident: Incident): Call<APIResponse>
}