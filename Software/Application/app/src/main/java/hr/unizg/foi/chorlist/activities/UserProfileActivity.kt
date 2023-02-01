package hr.unizg.foi.chorlist.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import hr.unizg.foi.chorlist.AUTH_PASSWORD
import hr.unizg.foi.chorlist.AUTH_SHARED_PREFERENCES
import hr.unizg.foi.chorlist.AUTH_USERNAME
import hr.unizg.foi.chorlist.databinding.ActivityUserProfileBinding
import hr.unizg.foi.chorlist.helpers.UpdateUserProfileDialogHelper
import hr.unizg.foi.chorlist.models.requests.UserUpdateRequest
import hr.unizg.foi.chorlist.models.responses.UserJwtResponse
import hr.unizg.foi.chorlist.models.responses.UserResponse
import hr.unizg.foi.chorlist.models.views.UserView
import hr.unizg.foi.chorlist.services.ChorlistService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.net.SocketTimeoutException

class UserProfileActivity : AppCompatActivity() {
    private lateinit var userProfileBinding: ActivityUserProfileBinding
    private lateinit var user: UserView
    private val userService = ChorlistService.userService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userProfileBinding = ActivityUserProfileBinding.inflate(layoutInflater)

        setContentView(userProfileBinding.root)

        setBackNavigation()

        showCurrentUserData()

        userProfileBinding.fabPendingFragmentCreateTask.setOnClickListener {

            UpdateUserProfileDialogHelper(this, user) {
                updateUserData(it)
                updateUserView(it)
            }
        }

        userProfileBinding.elements.btnLogout.setOnClickListener {
            logout()
        }
    }

    /**
     * Creates [UserView] instance
     *
     * @param userData represents new user data
     */
    private fun updateUserView(userData: UserUpdateRequest) {
        val updatedUserData = UserView(
            id = user.id,
            firstName = userData.firstName,
            lastName = userData.lastName,
            password = userData.lastName,
            email = user.email,
            username = user.username
        )
        showUserView(updatedUserData)
    }

    /**
     * Updates user data
     *
     * @param newUserData
     */
    private fun updateUserData(newUserData: UserUpdateRequest) =
        lifecycleScope.launch {
            val userToken = tryGetUserJwt()
            userToken?.let { tryUpdateUserData(newUserData, it) }
        }

    /**
     * Sends HTTP request to perform update of user data
     *
     */
    private suspend fun tryUpdateUserData(userData: UserUpdateRequest, token: String): Unit? {
        val response: Response<Unit> =
            try {
                userService.updateUser(userData, token)
            } catch (ex: SocketTimeoutException) {
                Toast.makeText(
                    baseContext, "Service currently not available", Toast.LENGTH_LONG
                ).show()
                redirectToLogin()
                return null
            }

        return checkResponse(response)
    }

    /**
     * Creates callback for back navigation button
     *
     * @return
     */
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()

        return true
    }

    /**
     * Sets back navigation
     *
     */
    private fun setBackNavigation() {
        val toolbar: Toolbar = userProfileBinding.userProfileToolbar

        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            setDisplayShowTitleEnabled(false)
        }
    }

    /**
     * Displays current logged user data
     *
     */
    private fun showCurrentUserData() {
        lifecycleScope.launch {
            val userResponse = getUserData()

            userResponse?.let {
                user = UserView(
                    id = userResponse.id,
                    firstName = userResponse.firstName,
                    lastName = userResponse.lastName,
                    username = userResponse.username,
                    password = userResponse.password,
                    email = userResponse.email,
                )
                showUserView(user)
            }
        }
    }

    /**
     * Returns JWT token of current logged user
     *
     * @return
     */
    private suspend fun getUserToken(): String? =
        withContext(Dispatchers.IO) {
            return@withContext tryGetUserJwt()
        }

    /**
     * Returns data of current logged user
     *
     * @return
     */
    private suspend fun getUserData(): UserResponse? =
        withContext(Dispatchers.IO) {
            return@withContext getUserToken()?.let { tryGetUserData(it) }
        }

    /**
     * Binds new user data to View
     *
     * @param userData user data to be bound to the View
     */
    private fun showUserView(userData: UserView) {
        userProfileBinding.apply {
            elements.tvLastName.text = userData.lastName
            elements.tvEmail.text = userData.email
            elements.tvFirstName.text = userData.firstName
            userProfileBinding.tvUsername.text = userData.username
        }
    }

    /**
     * Sends HTTP request to fetch current logged user data
     *
     */
    private suspend fun tryGetUserData(token: String): UserResponse? {
        val response: Response<UserResponse> =
            try {
                userService.getUser(token)
            } catch (ex: SocketTimeoutException) {
                Toast.makeText(
                    baseContext, "Service currently not available", Toast.LENGTH_LONG
                ).show()
                redirectToLogin()
                return null
            }
        checkResponse(response)

        return response.body()
    }

    /**
     * Sends HTTP request to fetch current logged user JWT
     *
     */
    private suspend fun tryGetUserJwt(): String? {
        val response: Response<UserJwtResponse> =
            try {
                userService.getJWT()
            } catch (ex: SocketTimeoutException) {
                Toast.makeText(
                    baseContext, "Service currently not available", Toast.LENGTH_LONG
                ).show()
                redirectToLogin()
                return null
            }
        checkResponse(response)

        return response.body()?.token
    }

    /**
     * Checks if response is successful
     *
     */
    private fun checkResponse(response: Response<*>) {
        if (response.isSuccessful.not()) {
            Toast.makeText(baseContext, "Please login", Toast.LENGTH_LONG).show()
            redirectToLogin()
        }
    }

    /**
     * Redirects user to login
     *
     */
    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    /**
     * Logout current logged user
     *
     */
    private fun logout() =
        lifecycleScope.launch {
            tryLogout()
            logoutSharedPreferences()
        }

    /**
     * Sends HTTP request to logout current logged user
     *
     */
    private suspend fun tryLogout() {
        try {
            userService.logout()
        } catch (ex: SocketTimeoutException) {
            Toast.makeText(
                baseContext, "Service currently not available", Toast.LENGTH_LONG
            ).show()
            return redirectToLogin()
        }
        Toast.makeText(
            baseContext, "Logged out successfully", Toast.LENGTH_LONG
        ).show()
        return redirectToLogin()
    }

    /**
     * Logs out the user from shared preferences.
     */
    private fun logoutSharedPreferences() =
        getSharedPreferences(AUTH_SHARED_PREFERENCES, Context.MODE_PRIVATE)
            .edit().apply {
                putString(AUTH_USERNAME, "")
                putString(AUTH_PASSWORD, "")
            }.apply()
}