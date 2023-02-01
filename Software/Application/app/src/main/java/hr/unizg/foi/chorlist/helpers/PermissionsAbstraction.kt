package hr.unizg.foi.chorlist.helpers

/**
 * Represent permission custom event listener
 *
 * @constructor Create empty Permissions abstraction instance
 */
class PermissionsAbstraction {
    private var listener: PermissionsConfirmedListener? = null
    var permissionsDeniedCount = 0

    /**
     * Interface used for specifying callback methods
     *
     *
     */
    interface PermissionsConfirmedListener {
        fun onExternalStorageConfirm()
    }

    /**
     * Set custom object listener
     *
     * @param listener setter for specific listener
     */
    fun setCustomObjectListener(listener: PermissionsConfirmedListener) {
        this.listener = listener
    }

    /**
     * Remove listener
     *
     */
    fun removeListener() {
        listener = null
    }

    /**
     * Method to invoke callback function
     *
     */
    fun startExternalStorageCallback() {
        listener?.onExternalStorageConfirm()
    }

    /**
     * Set permission denied counter
     *
     */
    fun setPermissionDenied() {
        permissionsDeniedCount++
    }
}