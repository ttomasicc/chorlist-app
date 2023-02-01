package hr.unizg.foi.chorlist.helpers

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import hr.unizg.foi.chorlist.R
import hr.unizg.foi.chorlist.databinding.DialogAddItemBinding
import hr.unizg.foi.chorlist.models.requests.ItemRequest
import hr.unizg.foi.chorlist.models.views.ShoppingListView
import kotlinx.coroutines.runBlocking

/**
 * Helper dialog class that manages adding new items.
 *
 * @property context context to which the dialog should be tied to
 * @property currentShoppingList ID of the shopping list which should be automatically preselected
 * @property addAction action to execute when the positive button is pressed
 */
class AddItemDialogHelper(
    private val context: Context,
    private val currentShoppingList: Long,
    private val addAction: (item: ItemRequest) -> Unit
) : ItemDialogHelper<DialogAddItemBinding>(
    DialogAddItemBinding.inflate(LayoutInflater.from(context))
) {
    init {
        runBlocking {
            val availableShoppingLists = getAvailableShoppingLists()
            binding.spnShoppingLists.apply {
                adapter = ArrayAdapter(
                    context,
                    android.R.layout.simple_spinner_dropdown_item,
                    availableShoppingLists
                )
                setSelection(availableShoppingLists.indexOfFirst { it.id == currentShoppingList })
            }
            showItemDialog()
        }
    }

    override fun showItemDialog(): AlertDialog =
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.add_item))
            .setView(binding.root)
            .setPositiveButton(context.getString(R.string.save)) { dialog, _ ->
                val itemRequest = buildItem()
                addAction(itemRequest)
                dialog.dismiss()
            }
            .setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
                dialog.cancel()
            }
            .create().apply {
                window?.setBackgroundDrawableResource(R.drawable.dialog_rounded_bg)
                show()
                enableInputValidation(
                    binding.etItemDescription,
                    getButton(AlertDialog.BUTTON_POSITIVE)
                )
            }

    /**
     * Builds the [ItemRequest] object from the data given in the dialog.
     *
     * @return [ItemRequest] object
     */
    private fun buildItem() = ItemRequest(
        idShoppingList = (binding.spnShoppingLists.selectedItem as ShoppingListView).id,
        description = binding.etItemDescription.text.toString().trim()
    )
}