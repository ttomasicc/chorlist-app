package hr.unizg.foi.chorlist.helpers

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import hr.unizg.foi.chorlist.R
import hr.unizg.foi.chorlist.databinding.DialogUpdateItemBinding
import hr.unizg.foi.chorlist.models.views.ItemView
import kotlinx.coroutines.runBlocking

/**
 * Helper dialog class that manages updating existing items.
 *
 * @property context context to which the dialog should be tied to
 * @property item the selectd item (used for pre-populating the dialog inputs)
 * @property updateAction the action to execute when the positive button is pressed
 */
class UpdateItemDialogHelper(
    private val context: Context,
    private val item: ItemView,
    private val updateAction: (item: ItemView) -> Unit
) : ItemDialogHelper<DialogUpdateItemBinding>(
    DialogUpdateItemBinding.inflate(LayoutInflater.from(context))
) {
    init {
        runBlocking {
            val shoppingListItems = getShoppingList(item.idShoppingList)
            binding.tvShoppingListName.text = shoppingListItems?.description ?: "Unknown"
            showItemDialog()
        }
    }

    override fun showItemDialog(): AlertDialog =
        AlertDialog.Builder(context)
            .setTitle("${context.getString(R.string.update_item)} - ${item.description}")
            .setView(binding.root)
            .setPositiveButton(context.getString(R.string.update)) { dialog, _ ->
                val itemView = buildItem()
                updateAction(itemView)
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
     * Modifies the item on which the update dialog was invoked.
     *
     * @return modified [ItemView] object
     */
    private fun buildItem() = item.apply {
        description = binding.etItemDescription.text.toString()
    }
}