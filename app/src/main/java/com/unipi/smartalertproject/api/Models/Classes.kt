package com.unipi.smartalertproject.api.Models

import com.google.gson.annotations.SerializedName

data class APIResponse(val result: Any?, val errorMessages:List<String>){

}

class LoginInfo(val username: String, val password: String) {

}

data class Tokens(val accessToken: String, val refreshToken: String) {
}

class RegisterInfo(val username: String, val firstname: String, val lastname: String,
                   val phoneNumber: String, val email: String, val password: String){
}

data class ValidationProblem(
    @SerializedName("type") val type: String,
    @SerializedName("title") val title: String,
    @SerializedName("status") val status: Int,
    @SerializedName("errors") val errors: Map<String, List<String>>
)