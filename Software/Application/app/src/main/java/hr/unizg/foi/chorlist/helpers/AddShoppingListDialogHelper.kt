package hr.unizg.foi.chorlist.helpers

import android.app.AlertDialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import hr.unizg.foi.chorlist.R
import hr.unizg.foi.chorlist.databinding.DialogAddUpdateShoppingListBinding
import hr.unizg.foi.chorlist.models.requests.ShoppingListRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * [AlertDialog] used for adding specific shopping list
 *
 * @property bind bind represents ViewBinding instance to fetch view
 * @property context represents state of passed object
 * @property addAction callback function to add specific shopping list
 * @constructor Create empty Add shopping list dialog helper instance
 */
class AddShoppingListDialogHelper(
    private val bind: DialogAddUpdateShoppingListBinding,
    private val context: Context,
    private val addAction: (shoppingList: ShoppingListRequest) -> Unit,
) : ShoppingListDialogHelper() {

    init {
        CoroutineScope(Dispatchers.Main).launch {
            showDialog()
        }
    }

    override val binding: DialogAddUpdateShoppingListBinding
        get() = bind

    /**
     * Show [AlertDialog] used to manage adding shopping list
     *
     * @return
     */
    override fun showDialog(): AlertDialog {
        val oldColor = (binding.pickedColor.background as ColorDrawable).color

        val builder = AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.add_shopping_list))
            .setView(binding.root)
            .setPositiveButton(context.getString(R.string.save)) { dialog, _ ->
                val shoppingListRequest = buildShoppingList()
                addAction(shoppingListRequest)
                dialog.dismiss()
            }
            .setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .setNeutralButton(context.getString(R.string.shopping_list_color), null)
        val dialog = builder.create()

        return dialog.apply {
            window?.setBackgroundDrawableResource(R.drawable.dialog_rounded_bg)
            show()
            val buttonPositive = getButton(AlertDialog.BUTTON_POSITIVE)
            val button = getButton(AlertDialog.BUTTON_NEUTRAL)

            button.setOnClickListener {
                ColorPickerDialogHelper(binding, context, oldColor, null)
            }

            enableInputValidation(
                binding.etShoppingListDescription,
                buttonPositive
            )
        }
    }

    /**
     * Build [ShoppingListRequest] instance
     *
     */
    private fun buildShoppingList() = ShoppingListRequest(
        description = binding.etShoppingListDescription.text.toString().trim(),
        color = String.format(
            "#%06X",
            (0xFFFFFF and (binding.pickedColor.background as ColorDrawable).color)
        )
    )
}