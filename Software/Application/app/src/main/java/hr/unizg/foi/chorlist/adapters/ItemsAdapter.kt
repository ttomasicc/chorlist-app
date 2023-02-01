package hr.unizg.foi.chorlist.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import hr.unizg.foi.chorlist.databinding.ListItemItemBinding
import hr.unizg.foi.chorlist.helpers.UpdateItemDialogHelper
import hr.unizg.foi.chorlist.models.views.ItemView

/**
 * Adapter class used for adapting [ItemView] to the android [RecyclerView]
 *
 * @property items the list of items that should be managed by the [RecyclerView]
 * @property updateAction the update action to execute when the user clicks on the item
 */
class ItemsAdapter(
    private val items: MutableList<ItemView>,
    private val updateAction: (item: ItemView) -> Unit
) : RecyclerView.Adapter<ItemsAdapter.ItemViewHolder>() {

    /**
     * ItemViewHolder is used for binding [ItemView] objects to the layout.
     *
     * @property binding the binding to which the item should be bind to
     */
    inner class ItemViewHolder(
        private val binding: ListItemItemBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ItemView) {
            binding.tvItemDescription.text = item.description ?: "Unknown"

            binding.root.setOnClickListener {
                UpdateItemDialogHelper(binding.root.context, item) { itemView ->
                    updateAction.invoke(itemView)
                    items[adapterPosition].apply {
                        description = itemView.description
                    }
                    notifyItemChanged(adapterPosition)
                }
            }
        }
    }

    /**
     * Enables adding a new [ItemView] object to the adapter.
     *
     * @param item the item that should be added
     */
    fun add(item: ItemView) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }

    /**
     * Enables left swipe to delete action on the adapter.
     *
     * @param recyclerView the [RecyclerView] object to which the action should be tied to
     * @param deleteAction the delete action that should be executed after the [Snackbar] disappears
     */
    fun enableSwipeToDelete(
        recyclerView: RecyclerView,
        deleteAction: (item: ItemView, isEmpty: Boolean) -> Unit
    ) {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val deletedItem: ItemView = items[position]

                items.removeAt(position)
                notifyItemRemoved(position)

                var shouldBeDeleted = true
                Snackbar.make(
                    recyclerView, "Deleted" + " ${deletedItem.description}", Snackbar.LENGTH_LONG
                ).setAction("Undo") {
                    items.add(position, deletedItem)
                    notifyItemInserted(position)
                    shouldBeDeleted = false
                }.addCallback(
                    object : Snackbar.Callback() {
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            if (shouldBeDeleted)
                                deleteAction(deletedItem, items.isEmpty())
                        }
                    }
                ).show()
            }
        }).attachToRecyclerView(recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ItemViewHolder(
        ListItemItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) =
        holder.bind(items[position])

    override fun getItemCount() = items.size
}