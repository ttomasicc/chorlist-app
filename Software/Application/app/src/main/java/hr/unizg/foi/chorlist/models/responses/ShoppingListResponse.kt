package hr.unizg.foi.chorlist.models.responses

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import hr.unizg.foi.chorlist.services.LocalDateTimeDeserializer
import java.time.LocalDateTime

data class ShoppingListResponse(
    @SerializedName("id")
    val id: Long,
    @SerializedName("id_user")
    val idUser: Long,
    @SerializedName("description")
    val description: String,
    @SerializedName("modified")
    @JsonAdapter(LocalDateTimeDeserializer::class)
    val modified: LocalDateTime,
    @SerializedName("color")
    val color: String,
)