package hr.unizg.foi.chorlist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import hr.unizg.foi.chorlist.activities.HomeActivity
import hr.unizg.foi.chorlist.activities.LoginActivity
import hr.unizg.foi.chorlist.models.requests.UserLoginRequest
import hr.unizg.foi.chorlist.services.ChorlistService
import kotlinx.coroutines.launch
import retrofit2.Response
import java.net.SocketException

/**
 * Serves as an application startup script.
 */
class MainActivity : AppCompatActivity() {
    private val userService = ChorlistService.userService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = getLoggedUser()
        lifecycleScope.launch {
            tryLoginUser(user)
        }
    }

    /**
     * Fetches user from shared preferences.
     *
     * @return [UserLoginRequest] with decoded username and password
     */
    private fun getLoggedUser() =
        getSharedPreferences(AUTH_SHARED_PREFERENCES, Context.MODE_PRIVATE).let {
            val username = it.getString(AUTH_USERNAME, "") ?: ""
            val password = it.getString(AUTH_PASSWORD, "") ?: ""

            UserLoginRequest(
                username = String(Base64.decode(username.toByteArray(), Base64.DEFAULT)),
                password = String(Base64.decode(password.toByteArray(), Base64.DEFAULT))
            )
        }

    /**
     * Tries to login user. If successful, forwards the user to the HomeActivity, otherwise to
     * LoginActivity.
     * By doing this, the REST API "re-saves" the HTTP session therefore enabling familiar further
     * user workflow.
     *
     * @param userLoginRequest a user that wants to login
     */
    private suspend fun tryLoginUser(userLoginRequest: UserLoginRequest) {
        val response: Response<Unit>

        try {
            response = userService.login(userLoginRequest)
        } catch (ex: SocketException) {
            Toast.makeText(
                baseContext, R.string.service_not_available, Toast.LENGTH_LONG
            ).show()

            return startActivity(Intent(baseContext, LoginActivity::class.java))
        }

        if (response.isSuccessful)
            startActivity(Intent(baseContext, HomeActivity::class.java))
        else
            startActivity(Intent(baseContext, LoginActivity::class.java))
    }
}
