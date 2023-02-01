package hr.unizg.foi.chorlist.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import hr.unizg.foi.chorlist.INTENT_SHOPPING_LIST_ID
import hr.unizg.foi.chorlist.INTENT_SHOPPING_LIST_NAME
import hr.unizg.foi.chorlist.R
import hr.unizg.foi.chorlist.adapters.ItemsAdapter
import hr.unizg.foi.chorlist.databinding.ActivityItemsBinding
import hr.unizg.foi.chorlist.helpers.AddItemDialogHelper
import hr.unizg.foi.chorlist.models.requests.ItemRequest
import hr.unizg.foi.chorlist.models.responses.ItemResponse
import hr.unizg.foi.chorlist.models.views.ItemView
import hr.unizg.foi.chorlist.services.ChorlistService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Response
import java.net.SocketTimeoutException

/**
 * Responsible for managing items within the selected shopping list.
 */
class ItemsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityItemsBinding

    private var currentShoppingListId = -1L

    private val userService = ChorlistService.userService
    private val itemService = ChorlistService.itemService
    private val shoppingListService = ChorlistService.shoppingListService

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Gets the shopping list ID for which the items should be displayed
        currentShoppingListId = intent.getLongExtra(INTENT_SHOPPING_LIST_ID, -1)

        if (currentShoppingListId != -1L) {
            binding = ActivityItemsBinding.inflate(layoutInflater)
            setContentView(binding.root)
            configureToolbar()

            displayItems()
            enableAddingItems()
        } else {
            Toast.makeText(
                baseContext, getString(R.string.shopping_list_not_found), Toast.LENGTH_LONG
            ).show()
            finish()
        }

        swipeRefreshLayout = binding.refreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            displayItems()
        }
    }

    /**
     * Configures the toolbar: back navigation and activity title.
     */
    private fun configureToolbar() {
        setBackNavigation()
        setActivityTitle()
    }

    /**
     * Enables going back to the previous activity.
     */
    private fun setBackNavigation() =
        setSupportActionBar(binding.topAppBar).also {
            supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(true)
                setDisplayShowHomeEnabled(true)
            }
        }

    /**
     * Sets the activity title using the data given by the [Intent] that called the activity.
     */
    private fun setActivityTitle() {
        binding.topAppBar.title = "${getString(R.string.shopping_list)} - ${
            intent.getStringExtra(INTENT_SHOPPING_LIST_NAME)
        }"
    }

    /**
     * Used for displaying the items in the [RecyclerView]
     */
    private fun displayItems() =
        lifecycleScope.launch {
            binding.apply {
                val items = getItems().toMutableList()
                pbLoadingItems.visibility = View.GONE

                bindItems(items)
            }
            swipeRefreshLayout.isRefreshing = false
        }

    /**
     * Binds the items to the [RecyclerView]
     *
     * @param items items that should be bind to the [RecyclerView]
     */
    private fun bindItems(items: MutableList<ItemView>) =
        binding.apply {
            if (items.isNotEmpty()) {
                rvItems.apply {
                    adapter = getItemsAdapter(items)
                    layoutManager = LinearLayoutManager(binding.root.context)
                }
            } else {
                rvItems.visibility = View.GONE
                tvEmptyMessage.visibility = View.VISIBLE
            }
        }

    /**
     * Responsible for configuring the [ItemsAdapter] that will be used by the [RecyclerView]
     *
     * @param items items that should be managed by the [ItemsAdapter]
     * @return the configured [ItemsAdapter]
     */
    private fun getItemsAdapter(items: MutableList<ItemView>): ItemsAdapter {
        binding.apply {
            return ItemsAdapter(items) {
                lifecycleScope.launch { updateItem(it) }
            }.apply {
                enableSwipeToDelete(rvItems) { itemView, isEmpty ->
                    if (isEmpty) {
                        rvItems.visibility = View.GONE
                        tvEmptyMessage.visibility = View.VISIBLE
                    }
                    GlobalScope.launch { deleteItem(itemView.id) }
                }
            }
        }
    }

    /**
     * Configures the [FloatingActionButton] that will display the [AddItemDialogHelper] on click
     */
    private fun enableAddingItems() =
        binding.fabAddItem.setOnClickListener {
            AddItemDialogHelper(this@ItemsActivity, currentShoppingListId) { itemRequest ->
                lifecycleScope.launch {
                    addItem(itemRequest)?.let {
                        updateView(ItemView(it))
                    }
                }
            }
        }

    /**
     * Adds the given [item] to the [RecyclerView].
     *
     * @param item the item that should be added to the [RecyclerView]
     */
    private fun updateView(item: ItemView) =
        binding.apply {
            if (item.idShoppingList == currentShoppingListId) {
                rvItems.visibility = View.VISIBLE
                tvEmptyMessage.visibility = View.GONE

                val itemsAdapter = rvItems.adapter as ItemsAdapter?
                if (itemsAdapter != null)
                    itemsAdapter.add(item)
                else
                    bindItems(mutableListOf(item))
            } else
                Toast.makeText(
                    baseContext,
                    getString(R.string.item_successfully_added),
                    Toast.LENGTH_SHORT
                ).show()
        }

    /**
     * Calls the REST API service to get the list of all items for the given shopping list.
     * If the call is unsuccessful, it ends the current activity.
     *
     * @return [List<ItemView>] that contains all the items contained in the current shopping list
     */
    private suspend fun getItems(): List<ItemView> {
        val response = try {
            shoppingListService.get(
                id = currentShoppingListId,
                auth = userService.getJWT().body()?.token ?: "null"
            )
        } catch (ex: SocketTimeoutException) {
            Toast
                .makeText(baseContext, getString(R.string.service_not_available), Toast.LENGTH_LONG)
                .show()
            return listOf()
        }

        if (response.isSuccessful)
            response.body()?.let { res ->
                return res.items.map { ItemView(it) }
            }
        else
            finish()

        return listOf()
    }

    /**
     * Calls the REST API service to post the newly created [item].
     * If the call is unsuccessful, it ends the current activity.
     *
     * @return [ItemResponse] if successful, otherwise null
     */
    private suspend fun addItem(item: ItemRequest): ItemResponse? =
        addOrUpdateItem {
            itemService.add(
                item = item,
                auth = userService.getJWT().body()?.token ?: "null"
            )
        }

    /**
     * Calls the REST API service to update the modified [item].
     * If the call is unsuccessful, it ends the current activity.
     */
    private suspend fun updateItem(item: ItemView) =
        addOrUpdateItem {
            itemService.update(
                id = item.id,
                item = ItemRequest(item),
                auth = userService.getJWT().body()?.token ?: "null"
            )
        }

    /**
     * General method used for adding or updating items.
     *
     * @param T the type of expected response
     * @param action the action to execute - add or update
     * @return the expected type [T] if successful, otherwise null
     */
    private suspend fun <T> addOrUpdateItem(action: suspend () -> Response<T>): T? {
        val response = try {
            action()
        } catch (ex: SocketTimeoutException) {
            Toast
                .makeText(baseContext, getString(R.string.service_not_available), Toast.LENGTH_LONG)
                .show()
            return null
        }

        if (response.isSuccessful.not())
            finish()

        return response.body()
    }

    /**
     * Calls the REST API service to delete the selected item given its [id].
     * If the call is unsuccessful, it ends the current activity.
     */
    private suspend fun deleteItem(id: Long) {
        val response = try {
            itemService.delete(
                id = id,
                auth = userService.getJWT().body()?.token ?: "null"
            )
        } catch (ex: SocketTimeoutException) {
            return Toast
                .makeText(baseContext, getString(R.string.service_not_available), Toast.LENGTH_LONG)
                .show()
        }

        if (response.isSuccessful.not())
            finish()
    }
}