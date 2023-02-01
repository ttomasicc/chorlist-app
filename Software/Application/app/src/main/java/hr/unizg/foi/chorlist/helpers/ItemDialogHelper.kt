package hr.unizg.foi.chorlist.helpers

import android.app.AlertDialog
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.viewbinding.ViewBinding
import hr.unizg.foi.chorlist.R
import hr.unizg.foi.chorlist.models.views.ShoppingListItemsView
import hr.unizg.foi.chorlist.models.views.ShoppingListView
import hr.unizg.foi.chorlist.services.ChorlistService
import java.net.SocketTimeoutException

/**
 * The ItemDialogHelper class is a helper class used as an abstraction for all item-related dialogs.
 *
 * @param VB type of ViewBinding
 * @property binding used for accessing the root context of the given ViewBinding
 */
abstract class ItemDialogHelper<VB : ViewBinding>(protected val binding: VB) {

    protected abstract fun showItemDialog(): AlertDialog

    protected fun enableInputValidation(editText: EditText, button: Button) {
        button.isEnabled = false
        editText.apply {
            doAfterTextChanged {
                button.isEnabled = text.isNullOrBlank().not()
            }
        }
    }

    /**
     * Fetches a single shopping list ([ShoppingListItemsView]) for the given [shoppingListId].
     *
     * @param shoppingListId id of the shopping list that should be fetched
     * @return [ShoppingListItemsView] if the list was found, otherwise null.
     */
    protected suspend fun getShoppingList(shoppingListId: Long): ShoppingListItemsView? {
        val response = try {
            ChorlistService.shoppingListService.get(
                id = shoppingListId,
                auth = ChorlistService.userService.getJWT().body()?.token ?: "null"
            )
        } catch (ex: SocketTimeoutException) {
            Toast.makeText(
                binding.root.context,
                binding.root.context.getString(R.string.service_not_available),
                Toast.LENGTH_LONG
            ).show()
            return null
        }

        if (response.isSuccessful)
            response.body()?.let {
                return ShoppingListItemsView(it)
            }

        return null
    }

    /**
     * Fetches all shopping lists that the user currently has.
     *
     * @return [List<ShoppingListItemsView>] that represents all the shopping lists that the user
     * currently has.
     */
    protected suspend fun getAvailableShoppingLists(): List<ShoppingListView> {
        val response = try {
            ChorlistService.shoppingListService.getAll(
                auth = ChorlistService.userService.getJWT().body()?.token ?: "null"
            )
        } catch (ex: SocketTimeoutException) {
            Toast.makeText(
                binding.root.context,
                binding.root.context.getString(R.string.service_not_available),
                Toast.LENGTH_LONG
            ).show()
            return listOf()
        }

        if (response.isSuccessful)
            response.body()?.let { res ->
                return res.map { ShoppingListView(it) }
            }

        return listOf()
    }
}