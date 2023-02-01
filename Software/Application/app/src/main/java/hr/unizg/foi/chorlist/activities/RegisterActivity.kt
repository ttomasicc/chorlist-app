package hr.unizg.foi.chorlist.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import hr.unizg.foi.chorlist.databinding.ActivityRegisterBinding
import hr.unizg.foi.chorlist.models.requests.UserRegisterRequest
import hr.unizg.foi.chorlist.models.responses.ErrorResponse
import hr.unizg.foi.chorlist.services.ChorlistService
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * Responsible for registering new users.
 */
class RegisterActivity : AppCompatActivity() {
    private val userService = ChorlistService.userService
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            // Enables navigation to the Login screen if the user already has an account
            tvHaveAccount.setOnClickListener {
                startActivity(Intent(baseContext, LoginActivity::class.java))
            }

            // Calls the registerUser method by providing it with the form data
            btnRegister.setOnClickListener {
                registerUser(
                    UserRegisterRequest(
                        firstName = etFirstName.text.toString().trim(),
                        lastName = etLastName.text.toString().trim(),
                        username = etUsername.text.toString().trim(),
                        email = etEmail.text.toString().trim(),
                        password = etPassword.text.toString().trim(),
                    )
                )
            }
        }
    }

    /**
     * Tries to register the given user.
     *
     * @param userRegisterRequest User data that should be registered
     */
    private fun registerUser(userRegisterRequest: UserRegisterRequest) {
        if (validateUser(userRegisterRequest))
            lifecycleScope.launch {
                saveUser(userRegisterRequest)
            }
    }

    /**
     * Validates all form data using the give [UserRegisterRequest] object.
     *
     * @param userRegisterRequest the object that should be validated
     * @return true if validation passed, otherwise shows the corresponding error message and
     * returns false
     */
    private fun validateUser(userRegisterRequest: UserRegisterRequest): Boolean {
        var success = true

        if (userRegisterRequest.firstName.isEmpty()) {
            binding.etFirstName.error = "Please enter first name"
            success = false
        }

        if (userRegisterRequest.lastName.isEmpty()) {
            binding.etLastName.error = "Please enter last name"
            success = false
        }

        if (userRegisterRequest.username.isEmpty()) {
            binding.etUsername.error = "Please enter username"
            success = false
        }

        if (userRegisterRequest.email.isEmpty()) {
            binding.etEmail.error = "Please enter email"
            success = false
        } else if (Patterns.EMAIL_ADDRESS.matcher(userRegisterRequest.email).matches().not()) {
            binding.etEmail.error = "Please enter valid email"
            success = false
        }

        if (userRegisterRequest.password.isEmpty()) {
            binding.etPassword.error = "Please enter password"
            success = false
        } else if (userRegisterRequest.password.length < 5) {
            binding.etPassword.error = "Password must have at least 5 characters"
            success = false
        }

        return success
    }

    /**
     * Calls the REST API service and tries to register the user given by the [UserRegisterRequest]
     * object.
     * If the call is unsuccessful, it ends the current activity.
     *
     * @param userRegisterRequest the user that wants to register
     */
    private suspend fun saveUser(userRegisterRequest: UserRegisterRequest) {
        val response: Response<Unit>
        try {
            response = userService.register(userRegisterRequest)
        } catch (ex: SocketTimeoutException) {
            return Toast.makeText(
                baseContext, "Service currently not available",
                Toast.LENGTH_LONG
            ).show()
        }

        if (response.isSuccessful) {
            Toast
                .makeText(baseContext, "Registration successful", Toast.LENGTH_LONG)
                .show()
            startActivity(Intent(baseContext, LoginActivity::class.java))
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

            if (apiError?.error?.contains("username") == true)
                return Toast
                    .makeText(baseContext, "Username already exists.", Toast.LENGTH_LONG)
                    .show()
            if (apiError?.error?.contains("email") == true)
                return Toast
                    .makeText(baseContext, "Email already exists.", Toast.LENGTH_LONG)
                    .show()
        }
    }
}