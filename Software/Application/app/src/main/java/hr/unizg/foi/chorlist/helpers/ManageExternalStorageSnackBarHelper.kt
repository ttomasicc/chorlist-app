package hr.unizg.foi.chorlist.helpers

import android.content.Context
import android.view.View
import com.google.android.material.snackbar.Snackbar
import hr.unizg.foi.chorlist.R

/**
 * Manage external storage access confirmation
 *
 * @property context represents state of passed object
 * @property view [View] on which [Snackbar] would be bound
 * @property openSettings callback function to invoke [Snackbar]
 * @constructor Create empty Manage external storage snack bar helper instance
 */
class ManageExternalStorageSnackBarHelper(
    private val context: Context,
    private val view: View,
    private val openSettings: () -> Unit
) {

    init {
        showSnackBar()
    }

    /**
     * Show [Snackbar] binding it to specific view
     *
     */
    private fun showSnackBar() {
        Snackbar.make(
            view, context.getString(R.string.sb_permission_message), Snackbar.LENGTH_LONG
        ).setAction("Settings") {
            openSettings()
        }.show()
    }
}