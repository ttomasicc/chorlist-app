package hr.unizg.foi.chorlist.models.views

import hr.unizg.foi.chorlist.models.responses.ItemResponse

class ItemView(
    val id: Long,
    val idShoppingList: Long,
    var description: String?
) {
    constructor(itemResponse: ItemResponse) : this(
        id = itemResponse.id,
        idShoppingList = itemResponse.idShoppingList,
        description = itemResponse.description
    )
}