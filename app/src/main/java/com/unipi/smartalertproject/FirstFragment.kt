package com.unipi.smartalertproject

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.unipi.smartalertproject.api.ApiService
import com.unipi.smartalertproject.api.AuthManager
import com.unipi.smartalertproject.api.Models.APIResponse
import com.unipi.smartalertproject.api.Models.LoginInfo
import com.unipi.smartalertproject.api.RetrofitClient
import com.unipi.smartalertproject.databinding.FragmentFirstBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.gson.Gson


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {
    private val apiService = RetrofitClient.retrofit.create(ApiService::class.java)
    private var _binding: FragmentFirstBinding? = null
    private var authManager: AuthManager? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        authManager = AuthManager(requireContext())
        return binding.root


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.buttonLogin.setOnClickListener {
            login(it)
        }

        binding.buttonIncidents.setOnClickListener{
            // create call
            getIncidents(it)
        }

    }

    private fun getIncidents(view: View){
        val token = authManager?.getAccessToken()
        if (token != null && authManager != null){
            val call: Call<APIResponse> = apiService.getIncidents("Bearer $token")

            Log.e("Incidents button", "Incidents clicked!")

            // execute call and wait for response or fail
            call.enqueue(object : Callback<APIResponse> {
                override fun onResponse(call: Call<APIResponse>, response: Response<APIResponse>) {
                    if (response.isSuccessful) { // TODO incidents
                        authManager!!.getAccessToken()?.let { it1 -> Log.e("Access token", it1) }
                        authManager!!.getRefreshToken()?.let { it1 -> Log.e("Refresh token", it1) }
                    }
                    else {
                        // Access token has expired so we must refresh it
                        if (authManager!!.isAccessTokenExpired(token)){
                            Log.e("Token expiration", "Token expired")
                            refreshToken(view)
                            getIncidents(view)
                        }
                        Log.e("Incidents error code", response.code().toString())
                        Log.e("Incidents error", response.message().toString())
                        // You can check response.code() and response.message() for details
                    }
                }

                override fun onFailure(call: Call<APIResponse>, t: Throwable) {
                    // Handle failure here
                    Log.e("Api error",  t.message.toString())
                }
            })
        }
    }

    private fun login(view: View){
        // Get user data
        val name = binding.textName.text.toString()
        val password = binding.textPassword.text.toString()
        val loginData = LoginInfo(name, password)

        // create call
        val call: Call<APIResponse> = apiService.loginUser(loginData)

        // execute call and wait for response or fail
        call.enqueue(object : Callback<APIResponse> {

            override fun onResponse(call: Call<APIResponse>, response: Response<APIResponse>) {

                if (response.isSuccessful) { // Handle successful response here
                    val data: APIResponse? = response.body()

                    // Due to an error this must be converted to strings map
                    val tokenMap: Map<String, String>? = data?.result as Map<String, String>?

                    if (tokenMap != null){  // get access and refresh tokens from the response
                        val accessToken: String? = tokenMap["accessToken"]
                        val refreshToken: String? = tokenMap["refreshToken"]
                        // Store them in shared preferences storage
                        if (accessToken != null) {
                            Log.d("Access token login", accessToken)
                            authManager?.setAccessToken(accessToken)
                        }
                        if (refreshToken != null) {
                            Log.d("Refresh token login", refreshToken)
                            authManager?.setRefreshToken(refreshToken)
                        }
                    }

                }
                else {
                    // Get api error messages
                    val apiError = response.errorBody()?.string()
                    val gson = Gson()
                    val apiResponse: APIResponse? = gson.fromJson(apiError, APIResponse::class.java)
                    val errorMessages: List<String>? = apiResponse?.errorMessages

                    errorMessages?.get(0)?.let { Log.e("Api error login response", it) }
                    // You can check response.code() and response.message() for details
                }
            }

            override fun onFailure(call: Call<APIResponse>, t: Throwable) {
                // Handle failure here
                Log.e("Api error",  t.message.toString())
            }
        })
    }

    private fun refreshToken(view: View){
        val refreshToken = authManager?.getRefreshToken()
        if (refreshToken != null){
            val call: Call<APIResponse> = apiService.refreshToken(refreshToken)

            // execute call and wait for response or fail
            call.enqueue(object : Callback<APIResponse> {
                override fun onResponse(call: Call<APIResponse>, response: Response<APIResponse>) {
                    if (response.isSuccessful) { // new token has been generated
                        val ApiResponse: APIResponse? = response.body()
                        val newToken = ApiResponse?.result as String
                        authManager?.setAccessToken(newToken)
                        Log.d("New token", newToken)
                    }
                    else {
                        val apiError = response.errorBody()?.string()
                        val gson = Gson()
                        val apiResponse: APIResponse? = gson.fromJson(apiError, APIResponse::class.java)
                        val errorMessages: List<String>? = apiResponse?.errorMessages
                        if (errorMessages != null) {
                            Log.e("Refresh failure", errorMessages[0])
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

    private fun redirectToRegister(view: View){
        // TODO
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}