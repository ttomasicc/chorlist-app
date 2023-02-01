package hr.unizg.foi.chorlist.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import hr.unizg.foi.chorlist.AUTH_PASSWORD
import hr.unizg.foi.chorlist.AUTH_SHARED_PREFERENCES
import hr.unizg.foi.chorlist.AUTH_USERNAME
import hr.unizg.foi.chorlist.databinding.ActivityLoginBinding
import hr.unizg.foi.chorlist.models.requests.UserLoginRequest
import hr.unizg.foi.chorlist.models.responses.ErrorResponse
import hr.unizg.foi.chorlist.services.ChorlistService
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * Responsible for logging registered users to application.
 */
class LoginActivity : AppCompatActivity() {
    private val userService = ChorlistService.userService
    private lateinit var binding: ActivityLoginBinding

    /**
     * Enabling navigation to Register screen if the user didn't register. Calling loginUser
     * method and providing it form data.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            tvCreateAccount.setOnClickListener {
                startActivity(Intent(baseContext, RegisterActivity::class.java))
            }

            btnLogin.setOnClickListener {
                loginUser(
                    UserLoginRequest(
                        username = etUsername.text.toString().trim(),
                        password = etPassword.text.toString().trim()
                    )
                )
            }
        }
    }

    /**
     * Tries to login user.
     *
     * @param userLoginRequest User data that should be logged in
     */
    private fun loginUser(userLoginRequest: UserLoginRequest) {
        if (validateCredentials(userLoginRequest))
            lifecycleScope.launch {
                tryLoginUser(userLoginRequest)
            }
    }

    /**
     * Validates form data, username and password fields.
     *
     * @param userLoginRequest the object that should be validated
     * @return true if validation passed, otherwise shows the corresponding error message and
     * returns false
     */
    private fun validateCredentials(userLoginRequest: UserLoginRequest): Boolean {
        var success = true

        if (userLoginRequest.username.isEmpty()) {
            binding.etUsername.error = "Please enter username."
            success = false
        }

        if (userLoginRequest.password.isEmpty()) {
            binding.etPassword.error = "Please enter password."
            success = false
        }

        return success
    }

    /**
     * Calls the REST API service and tries to login the user given by the [UserLoginRequest]
     *object.
     * @param userLoginRequest user that wants to log in
     */
    private suspend fun tryLoginUser(userLoginRequest: UserLoginRequest) {
        val response: Response<Unit>

        try {
            response = userService.login(userLoginRequest)
        } catch (ex: SocketTimeoutException) {
            return Toast.makeText(
                baseContext, "Service currently not available",
                Toast.LENGTH_LONG
            ).show()
        }

        if (response.isSuccessful) {
            loginSharedPreferences(userLoginRequest)
            startActivity(Intent(baseContext, HomeActivity::class.java))
        } else {
            val apiError: ErrorResponse?

            try {
                apiError = response.errorBody()?.let {
                    ChorlistService.errorConverter.convert(it)
                }
            } catch (ex: IOException) {
                return Toast
                    .makeText(baseContext, "Unknown error occurred", Toast.LENGTH_LONG)
                    .show()
            }

            if (apiError?.error?.contains("user") == true)
                return Toast
                    .makeText(baseContext, "User not found.", Toast.LENGTH_LONG)
                    .show()

            if (apiError?.error?.contains("password") == true)
                return Toast
                    .makeText(baseContext, "Wrong password.", Toast.LENGTH_LONG)
                    .show()
        }
    }

    private fun loginSharedPreferences(userLoginRequest: UserLoginRequest) =
        getSharedPreferences(AUTH_SHARED_PREFERENCES, Context.MODE_PRIVATE)
            .edit().apply {
                putString(
                    AUTH_USERNAME,
                    Base64.encodeToString(userLoginRequest.username.toByteArray(),
                        Base64.DEFAULT)
                )
                putString(
                    AUTH_PASSWORD,
                    Base64.encodeToString(userLoginRequest.password.toByteArray(),
                        Base64.DEFAULT)
                )
            }.apply()
}