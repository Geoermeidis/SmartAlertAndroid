package com.unipi.smartalertproject.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface ApiService {
    @POST("api/login")
    fun loginUser(@Body loginInfo: LoginInfo): Call<APIResponse>
    @POST("api/register")
    fun registerUser(@Body registerInfo: RegisterInfo): Call<APIResponse>
    @POST("api/refresh-token")
    fun refreshToken(@Body refreshToken: String): Call<APIResponse>
    @GET("incidents")
    fun getIncidents(@Header("Authorization") accessToken: String): Call<IncidentAPIResponse>
    @PUT("incidents/create")
    fun submitIncident(@Header("Authorization") accessToken: String,
                       @Body incident: CreateIncidentDTO): Call<APIResponse>
    @PUT("incidents/updatestate")
    fun processIncident(@Header("Authorization") accessToken: String, @Query("id") id: String,
                        @Query("status") status: String): Call<APIResponse>
}