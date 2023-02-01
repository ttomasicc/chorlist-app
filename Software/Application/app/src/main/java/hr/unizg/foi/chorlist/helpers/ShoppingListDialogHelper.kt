package hr.unizg.foi.chorlist.helpers

import android.content.DialogInterface
import android.widget.Button
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import hr.unizg.foi.chorlist.databinding.DialogAddUpdateShoppingListBinding

/**
 * Shopping list dialog abstract class
 *
 * @constructor Create empty Shopping list dialog helper instance
 */
abstract class ShoppingListDialogHelper {

    private var isColorValid: Boolean = false

    protected abstract val binding: DialogAddUpdateShoppingListBinding

    protected abstract fun showDialog(): DialogInterface

    /**
     * Enable input validation
     *
     * @param editText editText on which to perform validation
     * @param button represents AlertDialog positive button
     */
    protected open fun enableInputValidation(
        editText: EditText,
        button: Button
    ) {
        binding.apply {
            editText.apply {
                button.isEnabled = false
                doAfterTextChanged {
                    button.isEnabled = text.isNullOrBlank().not().or(isColorValid)
                }
            }
        }
    }

    /**
     * Enable color validation
     *
     * @param button represents AlertDialog positive button
     * @param colorValidation represents color modification state
     */
    protected open fun enableColorValidation(button: Button, colorValidation: Boolean) {
        isColorValid = colorValidation
        button.isEnabled = colorValidation
    }
}