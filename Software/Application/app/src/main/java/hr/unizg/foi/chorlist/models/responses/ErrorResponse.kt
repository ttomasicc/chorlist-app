package hr.unizg.foi.chorlist.models.responses

import com.google.gson.annotations.SerializedName

data class ErrorResponse(
    @SerializedName("error")
    val error: String = ""
)