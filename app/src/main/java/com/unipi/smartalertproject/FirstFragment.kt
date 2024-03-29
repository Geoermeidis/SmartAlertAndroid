package com.unipi.smartalertproject

import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.unipi.smartalertproject.api.ApiService
import com.unipi.smartalertproject.api.AuthManager
import com.unipi.smartalertproject.api.APIResponse
import com.unipi.smartalertproject.api.LoginInfo
import com.unipi.smartalertproject.api.RetrofitClient
import com.unipi.smartalertproject.api.Utils
import com.unipi.smartalertproject.databinding.FragmentFirstBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class FirstFragment : Fragment() {

    private val apiService = RetrofitClient.retrofit.create(ApiService::class.java)
    private var _binding: FragmentFirstBinding? = null
    private var authManager: AuthManager? = null
    private val utils: Utils = Utils()

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        authManager = AuthManager(requireContext())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonLogin.setOnClickListener {
            login()
        }

        binding.buttonSignup.setOnClickListener{
            redirectToRegister()
        }

    }

    private fun login(){
        // Get user data
        val name = binding.textName.text.toString()
        val password = binding.textPassword.text.toString()
        val loginData = LoginInfo(username = name, password = password)

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
                            authManager?.setUserIdFromToken(accessToken)
                            authManager?.setUserRoleFromToken(accessToken)
                            authManager?.setRefreshTokenExpirationDate(Calendar.getInstance().timeInMillis)
                        }

                        if (refreshToken != null) {
                            Log.d("Refresh token login", refreshToken)
                            authManager?.setRefreshToken(refreshToken)
                            utils.showSuccessMessage(getString(R.string.loginSuccessMessage), Toast.LENGTH_SHORT, requireContext())
                            authManager?.getUserRole()?.let { Log.d("User role", it) }
                            authManager?.getUserId()?.let { Log.d("User id", it) }
                            authManager?.getRefreshTokenExpirationDate()?.let { Log.d("User exp", it.toString()) }

                            if (authManager?.getUserRole().equals("Civilian"))
                            {  // redirect to main menu
                                findNavController().navigate(R.id.action_FirstFragment_to_mainMenuCivilianFragment)
                            }
                            else
                            {
                                findNavController().navigate(R.id.action_FirstFragment_to_mainMenuOfficerFragment)
                            }
                        }
                    }

                }
                else {
                    // Get api error messages
                    val apiError = response.errorBody()?.string()
                    val apiResponse: APIResponse? = apiError
                        ?.let { utils.convertStringToObject<APIResponse?>(it) }

                    apiResponse?.errorMessages?.get(0)?.let {
                        utils.showMessage(getString(R.string.loginErrorHeader), it, requireContext())
                    }
                    // You can check response.code() and response.message() for details
                }
            }

            override fun onFailure(call: Call<APIResponse>, t: Throwable) {
                // Handle failure here
                Log.e("Api error",  t.message.toString())
            }
        })
    }

    private fun redirectToRegister(){
        findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}