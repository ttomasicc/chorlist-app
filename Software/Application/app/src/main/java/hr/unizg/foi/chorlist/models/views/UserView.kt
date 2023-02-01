package hr.unizg.foi.chorlist.models.views

import com.google.gson.annotations.SerializedName
import hr.unizg.foi.chorlist.models.responses.UserResponse
import retrofit2.Response

data class UserView(
    @SerializedName("id")
    val id: Long,
    @SerializedName("firstname")
    val firstName: String,
    @SerializedName("lastname")
    val lastName: String,
    @SerializedName("username")
    val username: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
) {
    constructor(userResponse: UserResponse) : this(
        id = userResponse.id,
        firstName = userResponse.firstName,
        lastName = userResponse.lastName,
        username = userResponse.username,
        email = userResponse.email,
        password = userResponse.password
    )
}