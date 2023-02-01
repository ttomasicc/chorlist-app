package hr.unizg.foi.chorlist.helpers

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import hr.unizg.foi.chorlist.ALLOWED_PASSWORD_SIZE
import hr.unizg.foi.chorlist.R
import hr.unizg.foi.chorlist.databinding.DialogUpdateUserProfileBinding
import hr.unizg.foi.chorlist.models.requests.UserUpdateRequest
import hr.unizg.foi.chorlist.models.views.UserView

/**
 * [AlertDialog] used for updating specific user
 *
 * @property context context on which to represent given ViewBinding
 * @property user user data that should be updated
 * @property updateAction callback function to update given [user]
 * @constructor Create empty Update user profile dialog helper instance
 */
class UpdateUserProfileDialogHelper(
    private val context: Context,
    private val user: UserView,
    private val updateAction: (user: UserUpdateRequest) -> Unit
) {
    private lateinit var userProfileUpdateBindingEditTextElements: MutableList<EditText>
    private var binding: DialogUpdateUserProfileBinding =
        DialogUpdateUserProfileBinding.inflate(LayoutInflater.from(context))

    init {
        showItemDialog()
    }

    /**
     * Show [AlertDialog] used to manage updating user
     *
     * @return
     */
    private fun showItemDialog(): AlertDialog =
        AlertDialog.Builder(context)
            .setCancelable(false)
            .setTitle(context.getString(R.string.update_profile))
            .setView(binding.root)
            .setPositiveButton(context.getString(R.string.update_profile)) { dialog, _ ->
                val newUserData = buildUser()
                updateAction(newUserData)
                dialog.dismiss()
            }
            .setNegativeButton(context.getString(R.string.cancel)) { dialog: DialogInterface, _ ->
                dialog.dismiss()
            }
            .create().apply {
                window?.setBackgroundDrawableResource(R.drawable.dialog_rounded_bg)
                show()
                val button = getButton(AlertDialog.BUTTON_POSITIVE)
                binding.apply {
                    enableInputValidation(etFirstNameUpdate, button)
                    enableInputValidation(etLastNameUpdate, button)
                    enableInputValidation(etPasswordUpdate, button)
                }
            }

    /**
     * Update specific user property
     *
     * @return
     */
    private fun buildUser() =
        UserUpdateRequest(
            firstName = binding.etFirstNameUpdate.text.toString()
                .ifEmpty { user.firstName },
            lastName = binding.etLastNameUpdate.text.toString()
                .ifEmpty { user.lastName },
            password = binding.etPasswordUpdate.text.toString()
                .ifEmpty { user.password })


    private fun enableInputValidation(editText: EditText, button: Button) {
        button.isEnabled = false
        editText.apply {
            doAfterTextChanged {
                populateProfileUpdateBindingEditTextElements()
                button.isEnabled = checkIfAllAreValid(userProfileUpdateBindingEditTextElements)
            }
        }
    }

    private fun populateProfileUpdateBindingEditTextElements() {
        binding.apply {
            userProfileUpdateBindingEditTextElements = arrayListOf(
                etFirstNameUpdate,
                etLastNameUpdate,
                etPasswordUpdate
            )
        }
    }

    private fun checkIfAllAreValid(userProfileUpdateBindingEditTextElements: List<EditText>): Boolean {
        var validatePassword = true
        var validateIsNotEmpty = false

        userProfileUpdateBindingEditTextElements.forEach {
            if (binding.etPasswordUpdate.id == it.id && it.text.isNotEmpty()) {
                validatePassword = (it.text.length < ALLOWED_PASSWORD_SIZE).not()
            }
            if (it.text.isNotEmpty()) validateIsNotEmpty = true
        }
        return validateIsNotEmpty && validatePassword
    }
}