package hr.unizg.foi.chorlist.adapters

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import hr.unizg.foi.chorlist.*
import hr.unizg.foi.chorlist.activities.ItemsActivity
import hr.unizg.foi.chorlist.databinding.DialogAddListToWidgetBinding
import hr.unizg.foi.chorlist.databinding.DialogAddUpdateShoppingListBinding
import hr.unizg.foi.chorlist.databinding.ShoppingListItemBinding
import hr.unizg.foi.chorlist.helpers.UpdateShoppingListDialogHelper
import hr.unizg.foi.chorlist.models.views.ShoppingListView

/**
 * Shopping list adapter
 *
 * @property shoppingLists represents fetched shopping lists
 * @property context represents state of passed object
 * @property updateAction callback function to update given shopping list
 * @constructor Create empty Shopping list adapter instance
 */
class ShoppingListAdapter(
    private val shoppingLists: MutableList<ShoppingListView>,
    private val context: Context,
    private val updateAction: (shoppingList: ShoppingListView) -> Unit
) : RecyclerView.Adapter<ShoppingListAdapter.ShoppingListViewHolder>() {

    private var updateClicked: Boolean = false

    inner class ShoppingListViewHolder(
        private val binding: ShoppingListItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        /**
         * In charge of providing binding data
         *
         * @param shoppingList data of specific View in [RecyclerView]
         */
        fun bind(shoppingList: ShoppingListView) {
            binding.apply {
                tvDescription.text = shoppingList.description
                pickedColor.setBackgroundColor(Color.parseColor(shoppingList.color))
                tvModified.text = shoppingList.modified.toLocalDate().toString()
            }

            binding.root.setOnClickListener {
                if (updateClicked) {
                    UpdateShoppingListDialogHelper(
                        DialogAddUpdateShoppingListBinding.inflate(LayoutInflater.from(context)),
                        context,
                        shoppingLists[adapterPosition]
                    ) { shoppingListView ->
                        updateAction(shoppingListView)
                        shoppingLists[adapterPosition].apply {
                            description = shoppingListView.description
                            color = shoppingListView.color
                            modified = shoppingListView.modified
                        }
                        notifyItemChanged(adapterPosition)
                    }
                } else {
                    val intent = Intent(context, ItemsActivity::class.java)
                        .putExtra(
                            INTENT_SHOPPING_LIST_ID, shoppingLists[adapterPosition].id
                        )
                        .putExtra(
                            INTENT_SHOPPING_LIST_NAME,
                            shoppingLists[adapterPosition].description
                        ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                    context.startActivity(intent)
                }
            }

            binding.root.setOnLongClickListener {
                val bind = DialogAddListToWidgetBinding.inflate(LayoutInflater.from(context))
                AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.shopping_list_dialog))
                    .setView(bind.root)
                    .setPositiveButton(context.getString(R.string.yes)) { dialog, _ ->
                        val shoppingListId: Int = shoppingList.id.toInt()
                        val shoppingListName: String = shoppingList.description

                        context.getSharedPreferences(
                            WIDGET_SHARED_PREFERENCES,
                            Context.MODE_PRIVATE
                        )
                            .edit()
                            .apply {
                                putInt(WIDGET_LIST_ID, shoppingListId)
                                putString(WIDGET_LIST_NAME, shoppingListName)
                            }.apply()
                        Toast.makeText(
                            context, TOAST_ADDED_TO_LIST, Toast.LENGTH_LONG
                        ).show()
                        dialog.dismiss()
                    }
                    .setNegativeButton(context.getString(R.string.no)) { dialog, _ ->
                        dialog.cancel()
                    }
                    .create().apply {
                        window?.setBackgroundDrawableResource(R.drawable.dialog_rounded_bg)
                        show()
                    }
                true
            }
        }
    }


    /**
     * Set update clicked
     *
     * @param updateMode
     */
    fun setUpdateClicked(updateMode: Boolean) {
        updateClicked = updateMode
    }

    /**
     * Add specific shopping list on [RecyclerView]
     *
     * @param newShoppingList
     */
    fun add(newShoppingList: ShoppingListView) {
        shoppingLists.add(newShoppingList)
        notifyItemInserted(shoppingLists.size - 1)
    }

    /**
     * In charge of deleting specific shopping list on swipe
     *
     * @param recyclerView represents [RecyclerView] on which to perform action
     * @param deleteAction callback function to delete given shopping list
     * @receiver
     */
    fun enableSwipeToDelete(
        recyclerView: RecyclerView,
        deleteAction: (item: ShoppingListView, isEmpty: Boolean) -> Unit
    ) {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val deletedItem: ShoppingListView = shoppingLists[position]

                shoppingLists.removeAt(position)
                notifyItemRemoved(position)

                var shouldBeDeleted = true
                Snackbar.make(
                    recyclerView, "Deleted" + " ${deletedItem.description}", Snackbar.LENGTH_LONG
                ).setAction("Undo") {
                    shoppingLists.add(position, deletedItem)
                    notifyItemInserted(position)
                    shouldBeDeleted = false
                }.addCallback(
                    object : Snackbar.Callback() {
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            if (shouldBeDeleted)
                                deleteAction(deletedItem, shoppingLists.isEmpty())
                        }
                    }
                ).show()
            }
        }).attachToRecyclerView(recyclerView)
    }

    /**
     * Create new ViewHolder for [RecyclerView]
     *
     * @param parent group into which the new View will be added
     * @param viewType view type of new View
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ShoppingListViewHolder(
        ShoppingListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    /**
     * Provides data which are bind to specific View displayed in [RecyclerView]
     *
     * @param holder ViewHolder which should be updated with specific data in data set of adapter
     * @param position position of element in specific data set of adapter
     */
    override fun onBindViewHolder(
        holder: ShoppingListAdapter.ShoppingListViewHolder,
        position: Int
    ) = holder.bind(shoppingLists[position])

    /**
     * Get item count held by adapter
     *
     */
    override fun getItemCount() = shoppingLists.size
}