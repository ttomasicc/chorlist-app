package hr.unizg.foi.chorlist.models.requests

import hr.unizg.foi.chorlist.models.views.ShoppingListView

data class ShoppingListRequest(
    val description: String?,
    val color: String?
) {
    constructor(shoppingListView: ShoppingListView) : this(
        description = shoppingListView.description,
        color = shoppingListView.color
    )
}

