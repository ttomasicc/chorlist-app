package hr.unizg.foi.chorlist.helpers

import android.content.Context
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import hr.unizg.foi.chorlist.*
import hr.unizg.foi.chorlist.databinding.FragmentShoppingListsBinding

/**
 * In charge for triggering HomeActivity options selection
 *
 * @property binding represents ViewBinding instance
 * @constructor Create empty HomeActivity options helper instance
 *
 * @param context represents state of passed object
 */
class HomeActivityOptionsHelper(
    context: Context,
    private val binding: FragmentShoppingListsBinding
) {

    private val showShoppingListUpdate: Animation =
        AnimationUtils.loadAnimation(context, R.anim.fab_shopping_list_update_show)
    private val hideShoppingListUpdate: Animation =
        AnimationUtils.loadAnimation(context, R.anim.fab_shopping_list_update_hide)
    private val showShoppingListAdd: Animation =
        AnimationUtils.loadAnimation(context, R.anim.fab_shopping_list_add_show)
    private val hideShoppingListAdd: Animation =
        AnimationUtils.loadAnimation(context, R.anim.fab_shopping_list_add_hide)
    private val showShoppingListPdf: Animation =
        AnimationUtils.loadAnimation(context, R.anim.fab_shopping_list_pdf_show)
    private val hideShoppingListPdf: Animation =
        AnimationUtils.loadAnimation(context, R.anim.fab_shopping_list_pdf_hide)
    var hidden: Boolean = true

    /**
     * Show HomeActivity options
     *
     */
    fun displayOptions() {
        binding.fabElements.apply {
            changeButtonVisibility(
                fabShoppingListUpdate,
                "+",
                showShoppingListUpdate,
                UPDATE_SHOPPING_LIST_BUTTON_RIGHT_MARGIN_OFFSET,
                UPDATE_SHOPPING_LIST_BUTTON_BOTTOM_MARGIN_OFFSET
            )
            changeButtonVisibility(
                fabShoppingListAdd,
                "+",
                showShoppingListAdd,
                ADD_SHOPPING_LIST_BUTTON_RIGHT_MARGIN_OFFSET,
                ADD_SHOPPING_LIST_BUTTON_BOTTOM_MARGIN_OFFSET
            )
            changeButtonVisibility(
                fabShoppingListPdf,
                "+",
                showShoppingListPdf,
                PDF_SHOPPING_LIST_BUTTON_RIGHT_MARGIN_OFFSET,
                PDF_SHOPPING_LIST_BUTTON_BOTTOM_MARGIN_OFFSET
            )
        }
        hidden = false
    }

    /**
     * Hide HomeActivity options
     *
     */
    fun hideOptions() {
        binding.fabElements.apply {
            changeButtonVisibility(
                fabShoppingListUpdate,
                "-",
                hideShoppingListUpdate,
                UPDATE_SHOPPING_LIST_BUTTON_RIGHT_MARGIN_OFFSET,
                UPDATE_SHOPPING_LIST_BUTTON_BOTTOM_MARGIN_OFFSET
            )
            changeButtonVisibility(
                fabShoppingListAdd,
                "-",
                hideShoppingListAdd,
                ADD_SHOPPING_LIST_BUTTON_RIGHT_MARGIN_OFFSET,
                ADD_SHOPPING_LIST_BUTTON_BOTTOM_MARGIN_OFFSET
            )
            changeButtonVisibility(
                fabShoppingListPdf,
                "-",
                hideShoppingListPdf,
                PDF_SHOPPING_LIST_BUTTON_RIGHT_MARGIN_OFFSET,
                PDF_SHOPPING_LIST_BUTTON_BOTTOM_MARGIN_OFFSET
            )
        }
        hidden = true
    }

    /**
     * Perform mathematical operations
     *
     * @return
     */
    private fun String.doMath(): (Int, Int) -> Int =
        when (this) {
            "+" -> Int::plus
            "-" -> Int::minus
            else -> error("Operator not supported in **this** context")
        }

    /**
     * Change button visibility
     *
     * @param fab represents [FloatingActionButton] which state will change
     * @param operator represents mathematical operator
     * @param animation represents animation to perform
     * @param offsetRight represents [FloatingActionButton] right offset
     * @param offsetBottom represents [FloatingActionButton] left offset
     */
    private fun changeButtonVisibility(
        fab: FloatingActionButton,
        operator: String,
        animation: Animation,
        offsetRight: Double,
        offsetBottom: Double
    ) =
        binding.fabElements.apply {
            val layoutParams = fab.layoutParams as FrameLayout.LayoutParams
            fab.apply {
                layoutParams.apply {
                    rightMargin =
                        operator.doMath()(rightMargin, (measuredWidth * offsetRight).toInt())
                    bottomMargin = operator.doMath()(
                        bottomMargin,
                        (measuredHeight * offsetBottom).toInt()
                    )
                }
                setLayoutParams(layoutParams)
                startAnimation(animation)
                isClickable = true
            }
        }
}