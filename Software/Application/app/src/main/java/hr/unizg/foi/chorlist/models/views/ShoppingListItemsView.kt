package hr.unizg.foi.chorlist.models.views

import hr.unizg.foi.chorlist.models.responses.ShoppingListItemsResponse

data class ShoppingListItemsView(
    val id: Long,
    val idUser: Long,
    val description: String,
    val modified: String,
    val color: String,
    val items: List<ItemView>
) {
    constructor(shoppingListItemsResponse: ShoppingListItemsResponse) : this(
        id = shoppingListItemsResponse.id,
        idUser = shoppingListItemsResponse.idUser,
        description = shoppingListItemsResponse.description,
        modified = shoppingListItemsResponse.modified,
        color = shoppingListItemsResponse.color,
        items = shoppingListItemsResponse.items.map { ItemView(it) }
    )
}