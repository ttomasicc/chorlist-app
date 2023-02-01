package hr.unizg.foi.chorlist.models.responses

import com.google.gson.annotations.SerializedName

data class ItemResponse(
    val id: Long,
    @SerializedName("id_shopping_list")
    val idShoppingList: Long,
    val description: String
)