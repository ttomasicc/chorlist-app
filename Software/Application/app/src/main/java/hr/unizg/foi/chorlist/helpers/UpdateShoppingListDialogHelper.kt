package hr.unizg.foi.chorlist.helpers

import android.app.AlertDialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import androidx.core.graphics.toColorInt
import hr.unizg.foi.chorlist.R
import hr.unizg.foi.chorlist.databinding.DialogAddUpdateShoppingListBinding
import hr.unizg.foi.chorlist.models.views.ShoppingListView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime

/**
 * [AlertDialog] used for updating specific shopping list
 *
 * @property bind represents ViewBinding instance to fetch view
 * @property context context on which to represent given ViewBinding
 * @property shoppingList shopping list item that should be updated
 * @property updateAction callback function to update given [shoppingList]
 * @constructor Create empty Update shopping list dialog helper instance
 */
class UpdateShoppingListDialogHelper(
    private val bind: DialogAddUpdateShoppingListBinding,
    private val context: Context,
    private val shoppingList: ShoppingListView,
    private val updateAction: (shoppingList: ShoppingListView) -> Unit
) : ShoppingListDialogHelper() {

    init {
        CoroutineScope(Dispatchers.Main).launch {
            binding.pickedColor.setBackgroundColor(shoppingList.color.toColorInt())
            showDialog()
        }
    }

    override val binding: DialogAddUpdateShoppingListBinding
        get() = bind

    /**
     * Show [AlertDialog] used to manage updating shopping list
     *
     * @return
     */
    override fun showDialog(): AlertDialog {
        val oldColor = (binding.pickedColor.background as ColorDrawable).color

        val builder = AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.update_shopping_list))
            .setView(binding.root)
            .setPositiveButton(context.getString(R.string.save)) { dialog, _ ->
                val shoppingListRequest = buildShoppingList()
                updateAction(shoppingListRequest)
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
            var colorValidation: Boolean

            button.setOnClickListener {
                val colorPickerDialogAbstraction = ColorPickerDialogAbstraction()
                val colorDialog =
                    ColorPickerDialogHelper(
                        binding,
                        context,
                        oldColor,
                        colorPickerDialogAbstraction
                    )
                colorPickerDialogAbstraction.setCustomObjectListener(object :
                    ColorPickerDialogAbstraction.MyColorEnvelopeListener {
                    override fun onMyColorSelected() {
                        colorValidation = colorDialog.getColorStateValidation()
                        enableColorValidation(buttonPositive, colorValidation)
                    }
                })
            }
            enableInputValidation(
                binding.etShoppingListDescription,
                buttonPositive
            )
        }
    }

    /**
     * Update specific shopping list property
     *
     * @return
     */
    private fun buildShoppingList(): ShoppingListView {
        val oldShoppingListView = shoppingList.copy()

        return shoppingList.apply {
            description = binding.etShoppingListDescription.text.toString().trim()
            color = String.format(
                "#%06X",
                (0xFFFFFF and (binding.pickedColor.background as ColorDrawable).color)
            )
            modified = LocalDateTime.now()

            if (description.isEmpty()) description = oldShoppingListView.description
        }
    }
}