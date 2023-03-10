package hr.unizg.foi.chorlist.models.requests

import com.google.gson.annotations.SerializedName

data class UserLoginRequest(
    @SerializedName("username")
    val username: String,
    @SerializedName("password")
    val password: String
)
