package hr.unizg.foi.chorlist.models.responses

import com.google.gson.annotations.SerializedName

data class ShoppingListItemsResponse(
    val id: Long,
    @SerializedName("id_user")
    val idUser: Long,
    val description: String,
    val modified: String,
    val color: String,
    val items: List<ItemResponse>
)