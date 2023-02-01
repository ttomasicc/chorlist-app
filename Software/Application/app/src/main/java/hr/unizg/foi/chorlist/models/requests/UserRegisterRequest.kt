package hr.unizg.foi.chorlist.models.requests

import com.google.gson.annotations.SerializedName

data class UserRegisterRequest(
    @SerializedName("firstname")
    val firstName: String,
    @SerializedName("lastname")
    val lastName: String,
    @SerializedName("username")
    val username: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String,
)