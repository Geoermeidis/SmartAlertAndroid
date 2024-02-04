package com.unipi.smartalertproject.api

import android.content.Context
import android.content.SharedPreferences
import java.util.Calendar
import android.util.Log
import android.view.View
import com.unipi.smartalertproject.Utils
import com.unipi.smartalertproject.api.Models.APIResponse
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Base64

class AuthManager(val context: Context) {

    private var sharedPreferences: SharedPreferences = context.getSharedPreferences("com.unipi.smartalertproject.token_keys", Context.MODE_PRIVATE)
    private val utils: Utils = Utils()

    fun getAccessToken(): String? {
        return sharedPreferences.getString("accessToken", "")
    }

    fun setAccessToken(value: String) {
        with(sharedPreferences.edit()) {
            this?.putString("accessToken", value)
            this?.apply()
        } // TODO save the strings refreshToken to a string resource file
    }

    fun getRefreshToken(): String? {
        return sharedPreferences.getString("refreshToken", "")
    }

    fun setRefreshToken(value: String) {
        with(sharedPreferences.edit()) {
            this?.putString("refreshToken", value)
            this?.apply()
        } // TODO save the strings refreshToken to a string resource file
    }

    fun setUserId(value: String){
        with(sharedPreferences.edit()) {
            this?.putString("userId", value)
            this?.apply()
        } // TODO save the strings refreshToken to a string resource file
    }

    fun getUserId(): String? {
        return sharedPreferences.getString("userId", "")
    }

    fun setUserIdFromToken(token: String){
        val payload = decodeToken(token)
        setUserId(JSONObject(payload).getString("Id"))
    }

    fun setUserRole(value: String){
        with(sharedPreferences.edit()) {
            this?.putString("userRole", value)
            this?.apply()
        } // TODO save the strings refreshToken to a string resource file
    }

    fun getUserRole(): String?{
        return sharedPreferences.getString("userRole", "")
    }

    fun setUserRoleFromToken(token: String){
        val payload = decodeToken(token)
        setUserRole(JSONObject(payload).getString("Role"))  // TODO RIGHT JSON
    }

    fun isAccessTokenExpired(token: String): Boolean {
        val payload = decodeToken(token)
        val expirationDateLong: Long = JSONObject(payload).getString("exp").toLong()

        Log.e("Date expiring", expirationDateLong.toString())
        Log.e("Date current", Calendar.getInstance().time.time.toString())
        return expirationDateLong <= Calendar.getInstance().time.time  // expiration has passed
    }

    private fun decodeToken(jwt: String): String {
        val parts = jwt.split(".")
        return try {
            val charset = charset("UTF-8")
            // val header = String(Base64.getUrlDecoder().decode(parts[0].toByteArray(charset)), charset)
            val payload = String(Base64.getUrlDecoder().decode(parts[1].toByteArray(charset)), charset)
            // "$header"
            payload
        } catch (e: Exception) {
            "Error parsing JWT: $e"
        }
    }

    fun refreshToken(apiService: ApiService){
        val refreshToken = getRefreshToken()
        if (refreshToken != null){
            val call: Call<APIResponse> = apiService.refreshToken(refreshToken)

            // execute call and wait for response or fail
            call.enqueue(object : Callback<APIResponse> {
                override fun onResponse(call: Call<APIResponse>, response: Response<APIResponse>) {
                    if (response.isSuccessful) { // new token has been generated
                        val apiResponse: APIResponse? = response.body()
                        val newToken = apiResponse?.result as String
                        setAccessToken(newToken)
                        Log.d("New token", newToken)
                    }
                    else {
                        val apiError = response.errorBody()?.string()
                        val errors: APIResponse? = apiError?.let {
                            utils.convertStringToObject<APIResponse>(it) }
                        if (errors != null) {
                            Log.e("Refresh failure", errors.errorMessages[0])
                        }

                    }
                }

                override fun onFailure(call: Call<APIResponse>, t: Throwable) {
                    // Handle failure here
                    Log.e("Api error",  t.message.toString())
                }
            })
        }
    }

}