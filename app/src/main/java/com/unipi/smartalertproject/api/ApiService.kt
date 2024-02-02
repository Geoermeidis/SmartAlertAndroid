package com.unipi.smartalertproject.api

import com.unipi.smartalertproject.api.Models.LoginInfo
import com.unipi.smartalertproject.api.Models.APIResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("api/login")
    fun loginUser(@Body loginInfo: LoginInfo): Call<APIResponse>
    @POST("api/refresh-token")
    fun refreshToken(@Body refreshToken: String): Call<APIResponse>
    @GET("incidents")
    fun getIncidents(@Header("Authorization") accessToken: String): Call<APIResponse>
}