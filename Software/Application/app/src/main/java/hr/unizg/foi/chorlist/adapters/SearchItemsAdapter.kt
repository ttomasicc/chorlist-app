package hr.unizg.foi.chorlist.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import hr.unizg.foi.chorlist.INTENT_SHOPPING_LIST_ID
import hr.unizg.foi.chorlist.INTENT_SHOPPING_LIST_NAME
import hr.unizg.foi.chorlist.activities.ItemsActivity
import hr.unizg.foi.chorlist.databinding.ListItemSearchItemsResultBinding
import hr.unizg.foi.chorlist.models.views.ItemView
import hr.unizg.foi.chorlist.models.views.ShoppingListItemsView

/**
 * Adapter class used for adapting item search results to the android [RecyclerView]
 *
 * @property shoppingListItemsView the list of items that should be managed by the [RecyclerView]
 * @property updateAction the update action to execute when the user clicks on the item
 */
class SearchItemsAdapter(
    private var shoppingListItemsView: List<ShoppingListItemsView>,
    private val updateAction: (item: ItemView) -> Unit
) : RecyclerView.Adapter<SearchItemsAdapter.SearchItemResultViewHolder>() {

    /**
     * SearchItemResultViewHolder is used for binding [ShoppingListItemsView] objects to the layout.
     *
     * @property binding the binding to which the item should be bind to
     */
    inner class SearchItemResultViewHolder(
        private val binding: ListItemSearchItemsResultBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(shoppingListItemsView: ShoppingListItemsView) {
            binding.apply {
                tvShoppingListName.text = shoppingListItemsView.description
                tvShoppingListName.setOnClickListener {
                    val intent = Intent(this.root.context, ItemsActivity::class.java)
                        .putExtra(
                            INTENT_SHOPPING_LIST_ID, shoppingListItemsView.id
                        )
                        .putExtra(
                            INTENT_SHOPPING_LIST_NAME,
                            shoppingListItemsView.description
                        )
                    root.context.startActivity(intent)
                }
                rvItems.adapter = ItemsAdapter(
                    shoppingListItemsView.items.toMutableList(),
                    updateAction
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = SearchItemResultViewHolder(
        ListItemSearchItemsResultBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun onBindViewHolder(holder: SearchItemResultViewHolder, position: Int) =
        holder.bind(shoppingListItemsView[position])

    override fun getItemCount() = shoppingListItemsView.size
}