package com.unipi.smartalertproject

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.unipi.smartalertproject.api.ApiService
import com.unipi.smartalertproject.api.AuthManager
import com.unipi.smartalertproject.api.Models.APIResponse
import com.unipi.smartalertproject.api.Models.LoginInfo
import com.unipi.smartalertproject.api.Models.Tokens
import com.unipi.smartalertproject.api.RetrofitClient
import com.unipi.smartalertproject.databinding.FragmentFirstBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


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
            val token = authManager?.getAccessToken()
            if (token != null){
                val call: Call<APIResponse> = apiService.getIncidents("Bearer $token")

                Log.e("Button Incindets", "Incidents clicked!")

                // execute call and wait for response or fail
                call.enqueue(object : Callback<APIResponse> {
                    override fun onResponse(call: Call<APIResponse>, response: Response<APIResponse>) {
                        if (response.isSuccessful) { // Handle successful response here
                            authManager?.getAccessToken()?.let { it1 -> Log.e("ApiResponse", it1) }
                            authManager?.getRefreshToken()?.let { it1 -> Log.e("ApiResponse", it1) }
                        } else {
                            // Handle error response here
                            Log.e("Api response 2", response.code().toString())
                            Log.e("Api response 2", response.message().toString())
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

    }

    private fun login(view: View){
        // Get user data
        val name = binding.textName.text.toString()
        val password = binding.textPassword.text.toString()
        val loginData = LoginInfo(name, password)

        // create call
        val call: Call<APIResponse> = apiService.loginUser(loginData)

        Log.d("Button", "Clicked!")

        // execute call and wait for response or fail
        call.enqueue(object : Callback<APIResponse> {

            override fun onResponse(call: Call<APIResponse>, response: Response<APIResponse>) {

                if (response.isSuccessful) { // Handle successful response here
                    val data: APIResponse? = response.body()
                    Log.d("Api response", data?.result.toString())

                    // Due to an error this must be converted to strings map
                    val tokenMap: Map<String, String>? = data?.result as Map<String, String>?

                    if (tokenMap != null){  // get access and refresh tokens from the response
                        val accessToken: String? = tokenMap["accessToken"]
                        val refreshToken: String? = tokenMap["refreshToken"]
                        // Store them in shared preferences storage
                        if (accessToken != null) {
                            Log.e("API Response", accessToken)
                            authManager?.setAccessToken(accessToken)
                            authManager?.getAccessToken()?.let { Log.e("Token access", it) }
                        }
                        if (refreshToken != null) {
                            Log.e("API Response", refreshToken)
                            authManager?.setRefreshToken(refreshToken)
                            authManager?.getRefreshToken()?.let { Log.e("Refresh token", it) }
                        }
                    }

                } else {
                    // Handle error response here
                    Log.d("Api response", response.code().toString())
                    // You can check response.code() and response.message() for details
                }
            }

            override fun onFailure(call: Call<APIResponse>, t: Throwable) {
                // Handle failure here
                Log.e("Api error",  t.message.toString())
            }
        })
    }

    private fun redirectToRegister(view: View){
        // TODO
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}