package com.unipi.smartalertproject.api

import android.content.Context
import android.content.SharedPreferences
import java.util.Calendar
import android.util.Log
import com.unipi.smartalertproject.R
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Base64
import java.util.Date

class AuthManager(val context: Context) {

    private var sharedPreferences: SharedPreferences = context.getSharedPreferences("com.unipi.smartalertproject.token_keys", Context.MODE_PRIVATE)
    private val utils: Utils = Utils()

    fun getAccessToken(): String? {
        Log.d("Access token auth", sharedPreferences.getString(context.getString(R.string.accessToken), "")!!)
        return sharedPreferences.getString(context.getString(R.string.accessToken), "")
    }

    fun setAccessToken(value: String) {
        with(sharedPreferences.edit()) {
            this?.putString(context.getString(R.string.accessToken), value)
            this?.apply()
        }
    }

    private fun getRefreshToken(): String? {
        Log.d("Refresh token auth", sharedPreferences.getString(context.getString(R.string.refreshToken), "")!!)
        return sharedPreferences.getString(context.getString(R.string.refreshToken), "")
    }

    fun setRefreshToken(value: String) {
        with(sharedPreferences.edit()) {
            this?.putString(context.getString(R.string.refreshToken), value)
            this?.apply()
        }
    }

    fun setRefreshTokenExpirationDate(time: Long) {
        // set expiration in 7 days from now
        with(sharedPreferences.edit()) {
            this?.putLong(context.getString(R.string.refreshTokenExpirationDate), time + 24 * 60 * 60 * 1000L * 6)
            this?.apply()
        }
    }

    fun getRefreshTokenExpirationDate(): Long{
        Log.i("Date auth", sharedPreferences.getLong(context.getString(R.string.refreshTokenExpirationDate), 0).toString())
        return sharedPreferences.getLong(context.getString(R.string.refreshTokenExpirationDate), 0)
    }

    fun isRefreshTokenExpired(): Boolean {
        return Calendar.getInstance().timeInMillis > getRefreshTokenExpirationDate()
    }

    private fun setUserId(value: String){
        with(sharedPreferences.edit()) {
            this?.putString(context.getString(R.string.userid), value)
            this?.apply()
        }
    }

    fun setUserIdFromToken(token: String){
        val payload = decodeToken(token)
        setUserId(JSONObject(payload).getString(context.getString(R.string.jwtUserId)))
    }

    fun getUserId(): String? {
        Log.d("UserId auth", sharedPreferences.getString(context.getString(R.string.userid), "")!!)
        return sharedPreferences.getString(context.getString(R.string.userid), "")
    }

    private fun setUserRole(value: String){
        with(sharedPreferences.edit()) {
            this?.putString(context.getString(R.string.userrole), value)
            this?.apply()
        }
    }

    fun getUserRole(): String?{
        Log.d("User role auth", sharedPreferences.getString(context.getString(R.string.userrole), "")!!)
        return sharedPreferences.getString(context.getString(R.string.userrole), "")
    }

    fun setUserRoleFromToken(token: String){
        val payload = decodeToken(token)
        setUserRole(JSONObject(payload).getString(context.getString(R.string.jetUserRole)))
    }

    fun isAccessTokenExpired(token: String): Boolean {
        val payload = decodeToken(token)
        val expirationDate = Date(JSONObject(payload).getString(context.getString(R.string.expirationDate)).toLong() * 1000)
        val currentDate: Date = Calendar.getInstance().time

        Log.e("Date expiring", expirationDate.toString())
        Log.e("Date current", Calendar.getInstance().time.toString())

        return expirationDate <= currentDate // expiration has passed
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

    fun logout() {
        setUserRole("")
        setUserId("")
        setAccessToken("")
        setRefreshToken("")
        setRefreshTokenExpirationDate(0)
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