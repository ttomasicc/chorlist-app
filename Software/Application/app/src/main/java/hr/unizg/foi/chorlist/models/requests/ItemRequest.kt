package hr.unizg.foi.chorlist.models.requests

import com.google.gson.annotations.SerializedName
import hr.unizg.foi.chorlist.models.views.ItemView

data class ItemRequest(
    @SerializedName("id_shopping_list")
    val idShoppingList: Long,
    @SerializedName("description")
    val description: String? = null
) {
    constructor(itemView: ItemView) : this(
        idShoppingList = itemView.idShoppingList,
        description = itemView.description
    )
}