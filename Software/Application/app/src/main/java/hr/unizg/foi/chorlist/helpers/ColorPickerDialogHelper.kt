package hr.unizg.foi.chorlist.helpers

import android.content.Context
import android.content.DialogInterface
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import hr.unizg.foi.chorlist.R
import hr.unizg.foi.chorlist.databinding.DialogAddUpdateShoppingListBinding

/**
 * [ColorPickerDialog] used for selecting color property of specific shopping list
 *
 * @property bind bind represents ViewBinding instance to fetch view
 * @property context represents state of passed object
 * @property oldColor keeps track of old color property
 * @property colorPickerDialogAbstraction represents custom [ColorPickerDialog] event listener
 * @constructor Create empty Color picker dialog helper instance
 */
class ColorPickerDialogHelper(
    private val bind: DialogAddUpdateShoppingListBinding,
    private val context: Context,
    private val oldColor: Int,
    private val colorPickerDialogAbstraction: ColorPickerDialogAbstraction?
) : ShoppingListDialogHelper() {

    private var newColor: Int = 0

    init {
        showDialog()
    }

    override val binding: DialogAddUpdateShoppingListBinding
        get() = bind

    /**
     * Show [ColorPickerDialog] used to select shopping list color
     *
     * @return
     */
    override fun showDialog(): DialogInterface {
        return ColorPickerDialog.Builder(context, R.style.Theme_Chorlist)
            .setTitle(context.getString(R.string.shopping_list_color))
            .setPositiveButton(context.getString(R.string.save), ColorEnvelopeListener { envelope: ColorEnvelope?, _ ->
                envelope?.color?.let {
                    binding.pickedColor.setBackgroundColor(it)
                    newColor = it
                    colorPickerDialogAbstraction?.loadSelectedColor()
                }
            })
            .setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .attachAlphaSlideBar(true)
            .attachBrightnessSlideBar(true)
            .create().apply {
                show()
            }
    }

    /**
     * Check if color property changed in regards to [oldColor]
     *
     * @return
     */
    fun getColorStateValidation(): Boolean {
        return newColor != oldColor
    }
}