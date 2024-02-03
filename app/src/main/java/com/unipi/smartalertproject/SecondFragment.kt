package com.unipi.smartalertproject

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.unipi.smartalertproject.api.ApiService
import com.unipi.smartalertproject.api.AuthManager
import com.unipi.smartalertproject.api.Models.APIResponse
import com.unipi.smartalertproject.api.Models.RegisterInfo
import com.unipi.smartalertproject.api.Models.ValidationProblem
import com.unipi.smartalertproject.api.RetrofitClient
import com.unipi.smartalertproject.databinding.FragmentSecondBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {
    private val apiService = RetrofitClient.retrofit.create(ApiService::class.java)
    private var authManager: AuthManager? = null
    private var _binding: FragmentSecondBinding? = null
    private val utils: Utils = Utils()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonLoginInSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }

        binding.buttonSignUp.setOnClickListener{
            signup(it)
        }

        // Confirm passwords match
        binding.editConfirmPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // This method is called to notify you that characters within s are about to be replaced
                // with new text with a length of after.
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // This method is called to notify you that somewhere within s, the text has been
                // changed.
            }

            override fun afterTextChanged(s: Editable?) {
                // This method is called to notify you that the characters within s have been changed.
                // You can use s.toString() to get the updated text.
                val enteredText: String = s.toString()
                if (enteredText != binding.editSignUpPassword.text.toString()){
                    binding.textPassworderror.text = "Passwords dont match" // TODO add strings
                    binding.textPassworderror.setTextColor(Color.RED)
                }
                else{
                    binding.textPassworderror.text = "Passwords match"
                    binding.textPassworderror.setTextColor(Color.GREEN)
                }
            }
        })
    }

    private fun signup(view: View){
        // Get user data
        val firstname = binding.textFirstname.text.toString()
        val lastname = binding.extLastname.text.toString()
        val username = binding.textUsername.text.toString()
        val password = binding.editSignUpPassword.text.toString()
        val email = binding.textEmail.text.toString()
        val phone = binding.textPhone.text.toString()

        val registerInfo: RegisterInfo = RegisterInfo(username = username, firstname = firstname,
            lastname = lastname, password = password, email = email, phoneNumber = phone)

        // create call
        val call: Call<APIResponse> = apiService.registerUser(registerInfo)

        // Only make call if the passwords do match
        if (binding.editSignUpPassword.text.toString() == binding.editConfirmPassword.text.toString()){
            if (!areFieldsFull()){
                // execute call and wait for response or fail
                call.enqueue(object : Callback<APIResponse> {

                    override fun onResponse(call: Call<APIResponse>, response: Response<APIResponse>) {

                        if (response.isSuccessful) { // Handle successful response here
                            utils.showSuccessMessage("You have a created a new account!",
                                Toast.LENGTH_LONG, requireContext())
                            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
                        }
                        else {
                            // Get api error messages
                            handleRegistrationError(response)
                        }
                    }

                    override fun onFailure(call: Call<APIResponse>, t: Throwable) {
                        // Handle failure here
                        Log.e("Api error",  t.message.toString())
                    }
                })
            }
            else
                utils.showMessage("Required fields",
                    "Please fill out all fields before submitting", requireContext())
        }
    }

    private fun handleRegistrationError(response: Response<APIResponse>) {
        // Handle registration error here
        if (response.code() == 400) {
            // ValidationProblem response
            val errorBody = response.errorBody()?.string()
            val validationProblem: ValidationProblem? = errorBody?.let {
                utils.convertStringToObject<ValidationProblem>(it)
            }

            if (validationProblem?.type != null) {
                // Handle validation problem
                Log.e("Register error", "Validation error spotted")
                Log.e("Register error", utils.mapToString(validationProblem.errors))
                utils.showScrollableDialog(requireContext(), "Fields validation",
                    utils.mapToString(validationProblem.errors))
            }
            else {
                // No validation errors have appeared
                Log.e("Register error", "Error spotted")

                val apiResponse: APIResponse? = errorBody
                    ?.let { utils.convertStringToObject<APIResponse?>(it) }
                apiResponse?.errorMessages?.get(0)?.let {  utils.showMessage("Registration", it, requireContext()) }

            }
        }
    }

    fun areFieldsFull(): Boolean{
        val firstname = binding.textFirstname.text.toString()
        val lastname = binding.extLastname.text.toString()
        val username = binding.textUsername.text.toString()
        val password = binding.editSignUpPassword.text.toString()
        val email = binding.textEmail.text.toString()
        val phone = binding.textPhone.text.toString()
        Log.e("Null", firstname.isEmpty().toString())
        return firstname.isEmpty() && lastname.isEmpty() && username.isEmpty()
                && password.isEmpty() && email.isEmpty() && phone.isEmpty()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}