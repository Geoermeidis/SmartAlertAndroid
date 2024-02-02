package com.unipi.smartalertproject.api.Models

data class APIResponse(val result: Any?, val errorMessages:List<String>){

}

class LoginInfo(val username: String, val password: String) {

}

data class Tokens(val accessToken: String, val refreshToken: String) {
}