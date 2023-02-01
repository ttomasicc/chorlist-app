package hr.unizg.foi.chorlist.helpers

/**
 * Represent ColorPickerDialog custom event listener
 *
 * @constructor Create empty Color picker dialog abstraction instance
 */
class ColorPickerDialogAbstraction {

    private lateinit var listener: MyColorEnvelopeListener

    /**
     * Interface used for specifying callback methods
     *
     *
     */
    interface MyColorEnvelopeListener {
        fun onMyColorSelected()
    }

    /**
     * Set custom object listener
     *
     * @param listener setter for specific listener
     */
    fun setCustomObjectListener(listener: MyColorEnvelopeListener) {
        this.listener = listener
    }

    /**
     * Method to invoke callback function
     *
     */
    fun loadSelectedColor() {
        listener.onMyColorSelected()
    }
}