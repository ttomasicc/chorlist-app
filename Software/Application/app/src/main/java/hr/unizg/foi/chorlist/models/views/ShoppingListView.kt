package hr.unizg.foi.chorlist.models.views

import hr.unizg.foi.chorlist.models.responses.ShoppingListResponse
import java.time.LocalDateTime

data class ShoppingListView(
    val id: Long,
    val idUser: Long,
    var description: String,
    var modified: LocalDateTime,
    var color: String,
) {
    constructor(shoppingListResponse: ShoppingListResponse) : this(
        id = shoppingListResponse.id,
        idUser = shoppingListResponse.idUser,
        description = shoppingListResponse.description,
        modified = shoppingListResponse.modified,
        color = shoppingListResponse.color
    )

    override fun toString(): String = description
}