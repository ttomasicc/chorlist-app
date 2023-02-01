package hr.unizg.foi.chorlist.models.requests

import com.google.gson.annotations.SerializedName

data class UserUpdateRequest(
    @SerializedName("firstname")
    val firstName: String,
    @SerializedName("lastname")
    val lastName: String,
    @SerializedName("password")
    val password: String,
)