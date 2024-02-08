package com.unipi.smartalertproject


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
import com.unipi.smartalertproject.services.LocationService
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
    private val utils: Utils = Utils()
    private lateinit var locationService: LocationService
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

        binding.buttonSignup.setOnClickListener{
            findNavController().navigate(R.id.action_incidentsPreviewFragment2_to_mapsFragment)
            //redirectToRegister(it)
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
                            authManager?.setUserIdFromToken(accessToken)
                            authManager?.setUserRoleFromToken(accessToken)
                        }
                        if (refreshToken != null) {
                            Log.d("Refresh token login", refreshToken)
                            authManager?.setRefreshToken(refreshToken)
                            utils.showSuccessMessage("You have logged in!", Toast.LENGTH_SHORT, requireContext())
                            authManager?.getUserRole()?.let { Log.d("User role", it) }
                            authManager?.getUserId()?.let { Log.d("User id", it) }
                            if (authManager?.getUserRole().equals("Civilian"))
                            {  // redirect to submit new incident
                                // findNavController().navigate(R.id.action_FirstFragment_to_submitIncidentFragment2)
                                findNavController().navigate(R.id.action_FirstFragment_to_incidentStatsPreviewFragment)
                            }
                            else
                            {
                                findNavController().navigate(R.id.action_FirstFragment_to_incidentsPreviewFragment2)
                            }
                        }
                    }

                }
                else {
                    // Get api error messages
                    val apiError = response.errorBody()?.string()
                    val apiResponse: APIResponse? = apiError
                        ?.let { utils.convertStringToObject<APIResponse?>(it) }

                    apiResponse?.errorMessages?.get(0)?.let {  utils.showMessage("Login", it, requireContext()) }
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
        findNavController().navigate(com.unipi.smartalertproject.R.id.action_FirstFragment_to_SecondFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}