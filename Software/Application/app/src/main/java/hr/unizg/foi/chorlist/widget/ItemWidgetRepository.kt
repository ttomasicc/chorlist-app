package hr.unizg.foi.chorlist.widget

import android.content.Context
import hr.unizg.foi.chorlist.WIDGET_LIST_ID
import hr.unizg.foi.chorlist.WIDGET_SHARED_PREFERENCES
import hr.unizg.foi.chorlist.models.views.ItemView
import hr.unizg.foi.chorlist.services.ChorlistService.shoppingListService
import hr.unizg.foi.chorlist.services.ChorlistService.userService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import java.net.SocketTimeoutException
import kotlin.random.Random
import kotlin.random.nextLong

/**
 * Class represents repository for retrieving items of chosen shopping list.
 *
 * @property context represents state of passed object
 *
 * @param scope represents scope passed from ItemServiceContainer
 */
class ItemWidgetRepository(scope: CoroutineScope, private val context: Context) {

    data class Data(val name: String)

    /**
     * Creating a flow that runs indefinitely with random delay between 5s and 10s, and emitting
     * getItems method for retrieving items. Flow is observed and its scope is set from
     * ItemServiceContainer class.
     */
    val data: StateFlow<List<Data>> = flow {
        while (true) {
            delay(Random.nextLong(5000L..10000L))
            emit(getItems()
                .map { Data(it.description.toString()) }
            )
        }
    }.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = emptyList()
    )

    /**
     * Calls the REST API service to get the list of all items for the chosen shopping list.
     *
     * @return list of items of chosen shopping list
     */
    private suspend fun getItems(): List<ItemView> {

        val shoppingListId : Int =
            context.getSharedPreferences(WIDGET_SHARED_PREFERENCES, Context.MODE_PRIVATE)
                .getInt(WIDGET_LIST_ID, -1)

        val response = try {
            shoppingListService.get(
                id = shoppingListId.toLong(),
                auth = userService.getJWT().body()?.token ?: "null"
            )
        } catch (ex: SocketTimeoutException) {

            return listOf()
        }

        if (response.isSuccessful)
            response.body()?.let { res ->
                return res.items.map { ItemView(it) }
            }

        return listOf()
    }
}