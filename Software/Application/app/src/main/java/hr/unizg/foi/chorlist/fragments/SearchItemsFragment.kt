package hr.unizg.foi.chorlist.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import hr.unizg.foi.chorlist.R
import hr.unizg.foi.chorlist.adapters.SearchItemsAdapter
import hr.unizg.foi.chorlist.databinding.FragmentSearchItemsBinding
import hr.unizg.foi.chorlist.models.requests.ItemRequest
import hr.unizg.foi.chorlist.models.responses.ItemResponse
import hr.unizg.foi.chorlist.models.responses.ShoppingListItemsResponse
import hr.unizg.foi.chorlist.models.views.ItemView
import hr.unizg.foi.chorlist.models.views.ShoppingListItemsView
import hr.unizg.foi.chorlist.services.ChorlistService
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException

/**
 * [SearchItemsFragment] is responsible for searching all the user items (from all shopping lists).
 */
class SearchItemsFragment : Fragment() {
    private lateinit var binding: FragmentSearchItemsBinding

    private val userService = ChorlistService.userService
    private val itemService = ChorlistService.itemService
    private val shoppingListService = ChorlistService.shoppingListService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchItemsBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Manages the loading progress bar and forwards the given search [query] to the processing if
     * it is valid.
     *
     * @param query the query that should be processed
     */
    fun search(query: String) {
        binding.apply {
            tvNoItemsFound.isVisible = false
            pbSearchingItems.isVisible = true

            if (query.isBlank()) {
                pbSearchingItems.isVisible = false
                tvNoItemsFound.isVisible = true
                bindItems(null)
            } else {
                displayItems(query)
            }
        }
    }

    /**
     * Processes the given valid search [query] and binds it to the [RecyclerView].
     *
     * @param query the valid query that should be processed
     */
    private fun displayItems(query: String) =
        lifecycleScope.launch {
            val filteredItems = getFilteredItems(query)

            if (filteredItems.isNotEmpty()) {
                binding.tvNoItemsFound.isVisible = false
                bindItems(filteredItems)
            } else {
                binding.tvNoItemsFound.isVisible = true
                bindItems(null)
            }

            binding.pbSearchingItems.isVisible = false
        }

    /**
     * Binds the given [items] to the [RecyclerView] by mapping the data to the [SearchItemsAdapter]
     *
     * @param items the items that should be bind to the view
     */
    private fun bindItems(items: List<ShoppingListItemsView>?) {
        binding.rvSearchItemsResult.adapter = items?.let {
            SearchItemsAdapter(it) {
                lifecycleScope.launch { updateItem(it) }
            }
        }
    }

    /**
     * Searches all the items given the valid search [query]. It also fetches all the shopping lists
     * that contain the items that were found by the search and maps the items to their
     * corresponding shopping list.
     *
     * @param query the valid search query that should be processed
     * @return
     */
    private suspend fun getFilteredItems(query: String): List<ShoppingListItemsView> {
        val itemsResponse: List<ItemResponse> = searchItems(query)
        val shoppingListIdsUnique = itemsResponse.map { it.idShoppingList }.distinct()
        val shoppingListsItemsResponse = getShoppingLists(shoppingListIdsUnique)

        val items = itemsResponse.map { ItemView(it) }
        val shoppingLists = shoppingListsItemsResponse.map { ShoppingListItemsView(it) }

        return shoppingLists.map { shoppingList ->
            shoppingList.copy(items = items.filter { it.idShoppingList == shoppingList.id })
        }
    }

    /**
     * Calls the REST API service in parallel to fetch all shopping list given the list of their
     * ids.
     *
     * @param ids the list of shopping list id's that should be fetched
     * @return the list of all the found shopping lists
     */
    private suspend fun getShoppingLists(ids: List<Long>): List<ShoppingListItemsResponse> {
        val shoppingLists = ids.map { id ->
            lifecycleScope.async { getShoppingList(id) }
        }.awaitAll()

        return shoppingLists.filterNotNull()
    }

    /**
     * Calls the search items REST API response for the given valid search [query].
     *
     * @param query the valid search query that should be processed
     * @return the list of items that contain the given search query in their description
     */
    private suspend fun searchItems(query: String): List<ItemResponse> {
        val response = try {
            itemService.search(
                query = query,
                auth = userService.getJWT().body()?.token ?: "null"
            )
        } catch (ex: SocketTimeoutException) {
            Toast.makeText(
                binding.root.context,
                getString(R.string.service_not_available),
                Toast.LENGTH_LONG
            ).show()
            return listOf()
        }

        if (response.isSuccessful)
            response.body()?.let { res ->
                return res
            }

        return listOf()
    }

    /**
     * Fetches the shopping list given its [id].
     *
     * @param id the shopping list id
     * @return the [ShoppingListItemsResponse] if the call is successful, otherwise null
     */
    private suspend fun getShoppingList(id: Long): ShoppingListItemsResponse? {
        val response = try {
            shoppingListService.get(
                id = id,
                auth = userService.getJWT().body()?.token ?: "null"
            )
        } catch (ex: SocketTimeoutException) {
            Toast.makeText(
                binding.root.context,
                getString(R.string.service_not_available),
                Toast.LENGTH_LONG
            ).show()
            return null
        }

        return response.body()
    }

    /**
     * Calls the REST API service to update the modified [item].
     *
     * @param item the modified item that should be persisted
     * @return Unit if successful, otherwise null
     */
    private suspend fun updateItem(item: ItemView): Unit? {
        val response = try {
            itemService.update(
                id = item.id,
                item = ItemRequest(item),
                auth = userService.getJWT().body()?.token ?: "null"
            )
        } catch (ex: SocketTimeoutException) {
            Toast
                .makeText(
                    binding.root.context,
                    getString(R.string.service_not_available),
                    Toast.LENGTH_LONG
                )
                .show()
            return null
        }

        if (response.isSuccessful.not())
            return null

        return response.body()
    }
}