package hr.unizg.foi.chorlist.fragments

import android.Manifest.permission.*
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import hr.unizg.foi.chorlist.BuildConfig
import hr.unizg.foi.chorlist.PERMISSION_CODE
import hr.unizg.foi.chorlist.R
import hr.unizg.foi.chorlist.activities.LoginActivity
import hr.unizg.foi.chorlist.adapters.ShoppingListAdapter
import hr.unizg.foi.chorlist.databinding.DialogAddUpdateShoppingListBinding
import hr.unizg.foi.chorlist.databinding.FragmentShoppingListsBinding
import hr.unizg.foi.chorlist.helpers.*
import hr.unizg.foi.chorlist.models.requests.ShoppingListRequest
import hr.unizg.foi.chorlist.models.views.ShoppingListItemsView
import hr.unizg.foi.chorlist.models.views.ShoppingListView
import hr.unizg.foi.chorlist.services.ChorlistService
import kotlinx.coroutines.*
import retrofit2.Response
import java.net.SocketTimeoutException

class ShoppingListsFragment : Fragment() {

    private lateinit var binding: FragmentShoppingListsBinding
    private lateinit var options: HomeActivityOptionsHelper
    private val permissionsAbstraction = PermissionsAbstraction()
    private val userService = ChorlistService.userService
    private val shoppingListService = ChorlistService.shoppingListService
    private lateinit var shoppingLists: MutableList<ShoppingListView>
    private var updateClicked = false
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentShoppingListsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            fabElements.fabShoppingListPdf.setOnClickListener {
                requestPermission()
            }
        }

        options = HomeActivityOptionsHelper(binding.root.context, binding)

        swipeRefreshLayout = binding.refreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            displayShoppingLists()
        }

        displayShoppingLists()
        enableOptions()
        enableAddingShoppingLists()
        enableUpdatingShoppingLists()
    }

    /**
     * Pass fetched shopping lists that are bound to RecyclerView
     *
     */
    private fun displayShoppingLists() =
        lifecycleScope.launch {
            binding.apply {
                shoppingLists = getShoppingLists().toMutableList()

                pbLoadingShoppingLists.visibility = View.GONE

                bindShoppingLists(shoppingLists)
            }
            swipeRefreshLayout.isRefreshing = false
        }

    /**
     * Keep track of update/select shopping list option
     *
     */
    private fun enableOptions() =
        binding.fabShowAllOptions.setOnClickListener {
            if (updateClicked.not()) {
                options.run {
                    if (hidden) displayOptions()
                    else hideOptions()
                }
            } else {
                restoreOldButtonOptions()
                updateClicked = false
                (binding.rvShoppingLists.adapter as ShoppingListAdapter?)?.setUpdateClicked(
                    updateClicked
                )
            }
        }

    /**
     * Shows shopping list add AlertDialog
     *
     */
    private fun enableAddingShoppingLists() {
        binding.fabElements.fabShoppingListAdd.setOnClickListener {
            AddShoppingListDialogHelper(
                DialogAddUpdateShoppingListBinding.inflate(
                    LayoutInflater.from(binding.root.context)
                ), binding.root.context
            ) { shoppingListRequest ->
                lifecycleScope.launch {
                    addShoppingList(shoppingListRequest)?.let {
                        updateView(ShoppingListView(it))
                    }
                }
            }
        }
    }

    /**
     * Fetch all shopping lists with belonging items for current logged user
     *
     * @return
     */
    private suspend fun getShoppingListItems(): List<ShoppingListItemsView> {
        val shoppingListItems = mutableListOf<ShoppingListItemsView>()
        for (shoppingList: ShoppingListView in shoppingLists) {
            withContext(Dispatchers.IO) {
                return@withContext getItems(shoppingList.id)
            }?.let { shoppingListItems.add(it) }
        }
        return shoppingListItems
    }

    /**
     * Fetch all shopping list items for current logged user
     *
     * @return
     */
    private suspend fun getItems(shoppingListId: Long): ShoppingListItemsView? {
        val response = try {
            shoppingListService.get(
                id = shoppingListId,
                auth = userService.getJWT().body()?.token ?: "null"
            )
        } catch (ex: SocketTimeoutException) {
            Toast
                .makeText(
                    binding.root.context,
                    getString(R.string.service_not_available),
                    Toast.LENGTH_LONG
                )
                .show()
            return null
        }

        if (response.isSuccessful.not())
            redirectToLogin()

        return response.body()?.let {
            ShoppingListItemsView(it)
        }
    }

    /**
     * Shows shopping list update AlertDialog
     *
     */
    private fun enableUpdatingShoppingLists() {
        binding.fabElements.fabShoppingListUpdate.setOnClickListener {
            options.hideOptions()
            setCancelButton()
            updateClicked = true
            showUpdateMessage()
            (binding.rvShoppingLists.adapter as ShoppingListAdapter?)?.setUpdateClicked(
                updateClicked
            )
        }
    }

    private fun showUpdateMessage() =
        Toast
            .makeText(
                binding.root.context, "Select shopping list to update",
                Toast.LENGTH_LONG
            )
            .show()

    private fun setCancelButton() {
        binding.fabShowAllOptions.setImageResource(R.drawable.cancel_update)
    }

    private fun restoreOldButtonOptions() {
        binding.fabShowAllOptions.setImageResource((R.drawable.home_activity_options))
    }

    /**
     * Update specific [View] with [shoppingList] instance
     *
     * @param shoppingList represents data which will be bound to RecyclerView
     */
    private fun updateView(shoppingList: ShoppingListView) =
        binding.apply {
            rvShoppingLists.visibility = View.VISIBLE
            tvEmptyMessage.visibility = View.GONE
            (rvShoppingLists.adapter as ShoppingListAdapter?)?.add(shoppingList)
                ?: bindShoppingLists(mutableListOf(shoppingList))
            Toast.makeText(
                binding.root.context, "Shopping list succesfully added",
                Toast.LENGTH_SHORT
            ).show()
        }

    /**
     * Bind shopping lists to RecyclerView
     *
     * @param shoppingLists represents data managed by [ShoppingListAdapter]
     */
    private fun bindShoppingLists(shoppingLists: MutableList<ShoppingListView>) =
        binding.apply {
            if (shoppingLists.isNotEmpty()) {
                rvShoppingLists.apply {
                    adapter = getShoppingListsAdapter(shoppingLists)
                    layoutManager = LinearLayoutManager(binding.root.context)
                }
            } else {
                rvShoppingLists.visibility = View.GONE
                tvEmptyMessage.visibility = View.VISIBLE
            }
        }

    /**
     * In charge for configuring the [ShoppingListAdapter] that will be used by RecyclerView
     *
     * @param shoppingLists represents data managed by [ShoppingListAdapter]
     * @return
     */
    private fun getShoppingListsAdapter(shoppingLists: MutableList<ShoppingListView>):
            ShoppingListAdapter {
        binding.apply {
            return ShoppingListAdapter(shoppingLists, binding.root.context) {
                lifecycleScope.launch {
                    updateShoppingList(it)
                }
            }.apply {
                enableSwipeToDelete(rvShoppingLists) { shoppingListView, isEmpty ->
                    if (isEmpty) {
                        rvShoppingLists.visibility = View.GONE
                        tvEmptyMessage.visibility = View.VISIBLE
                    }
                    GlobalScope.launch { deleteShoppingList(shoppingListView.id) }
                }
            }
        }
    }

    /**
     * Fetch all shopping lists for current logged user
     *
     * @return
     */
    private suspend fun getShoppingLists(): List<ShoppingListView> {
        val response = try {
            shoppingListService.getAll(
                auth = userService.getJWT().body()?.token ?: "null"
            )
        } catch (ex: SocketTimeoutException) {
            Toast
                .makeText(
                    binding.root.context,
                    getString(R.string.service_not_available),
                    Toast.LENGTH_LONG
                )
                .show()
            return listOf()
        }

        if (response.isSuccessful)
            response.body()?.let { res ->
                return res.map { ShoppingListView(it) }
            }
        else
            redirectToLogin()

        return listOf()
    }

    /**
     * Add shopping list for current logged user
     *
     * @param shoppingList
     */
    private suspend fun addShoppingList(shoppingList: ShoppingListRequest) =
        addOrUpdateShoppingList {
            shoppingListService.add(
                auth = userService.getJWT().body()?.token ?: "null",
                shoppingListRequest = shoppingList
            )
        }

    /**
     * Update shopping list for current logged user
     *
     * @param shoppingList
     */
    private suspend fun updateShoppingList(shoppingList: ShoppingListView) =
        addOrUpdateShoppingList {
            shoppingListService.update(
                id = shoppingList.id,
                shoppingListRequest = ShoppingListRequest(shoppingList),
                auth = userService.getJWT().body()?.token ?: "null"
            )
        }

    /**
     * Add or update shopping list for current logged user
     *
     * @param action callback function to perform specific action on shopping list
     * @return
     */
    private suspend fun <T> addOrUpdateShoppingList(action: suspend () -> Response<T>): T? {
        val response = try {
            action()
        } catch (ex: SocketTimeoutException) {
            Toast
                .makeText(
                    binding.root.context,
                    getString(R.string.service_not_available),
                    Toast.LENGTH_LONG
                )
                .show()
            return null
        }
        if (response.isSuccessful.not())
            redirectToLogin()
        return response.body()
    }

    /**
     * Delete shopping list for current logged user
     *
     * @param id represents shopping list that will be deleted
     */
    private suspend fun deleteShoppingList(id: Long) {
        val response = try {
            shoppingListService.delete(
                id = id,
                auth = userService.getJWT().body()?.token ?: "null"
            )
        } catch (ex: SocketTimeoutException) {
            return Toast
                .makeText(
                    binding.root.context,
                    getString(R.string.service_not_available),
                    Toast.LENGTH_LONG
                )
                .show()
        }

        if (response.isSuccessful.not())
            this.redirectToLogin()
    }

    /**
     * Redirect user to login
     *
     */
    private fun redirectToLogin() {
        val intent = Intent(binding.root.context, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        activity?.finish()
    }

    /**
     * Set up listener for custom permission event listener
     *
     */
    override fun onResume() {
        super.onResume()

        permissionsAbstraction.startExternalStorageCallback()
    }

    /**
     * Check [RuntimePermission] of current logged user
     *
     * @return
     */
    private fun checkPermissions(): Boolean {

        val writeStoragePermission = ContextCompat.checkSelfPermission(
            binding.root.context,
            WRITE_EXTERNAL_STORAGE
        )

        val readStoragePermission = ContextCompat.checkSelfPermission(
            binding.root.context,
            READ_EXTERNAL_STORAGE
        )

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            (writeStoragePermission == PackageManager.PERMISSION_GRANTED).and(
                readStoragePermission == PackageManager.PERMISSION_GRANTED
            ).and(Environment.isExternalStorageManager())
        } else {
            (writeStoragePermission == PackageManager.PERMISSION_GRANTED).and(
                readStoragePermission == PackageManager.PERMISSION_GRANTED
            )
        }
    }

    /**
     * Request [RuntimePermission] to current logged user
     *
     */
    private fun requestPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requestPermissions(
                arrayOf(
                    READ_EXTERNAL_STORAGE,
                    WRITE_EXTERNAL_STORAGE,
                    MANAGE_EXTERNAL_STORAGE
                ),
                PERMISSION_CODE
            )
        } else {
            requestPermissions(
                arrayOf(
                    READ_EXTERNAL_STORAGE,
                    WRITE_EXTERNAL_STORAGE
                ),
                PERMISSION_CODE
            )
        }
    }

    /**
     * Determine if permissions are allowed
     *
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if ((requestCode != PERMISSION_CODE).and(grantResults.isEmpty())) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            when {

                checkForRuntimePermissions(grantResults).not() -> {

                    if (permissionsAbstraction.permissionsDeniedCount > 0) {
                        binding.fabElements.fabShoppingListPdf.apply {
                            backgroundTintList = ContextCompat.getColorStateList(
                                binding.root.context,
                                androidx.appcompat.R.color.material_blue_grey_800
                            )
                            isEnabled = false
                        }
                    } else {
                        permissionsAbstraction.setPermissionDenied()
                    }
                }

                checkForRuntimePermissions(grantResults).and(
                    Environment.isExternalStorageManager().not()
                ) -> {
                    ManageExternalStorageSnackBarHelper(binding.root.context, binding.root) {
                        val uri = Uri.parse("package:${BuildConfig.APPLICATION_ID}")

                        permissionsAbstraction.setCustomObjectListener(object :
                            PermissionsAbstraction.PermissionsConfirmedListener {
                            override fun onExternalStorageConfirm() {
                                if (checkPermissions()) {
                                    Toast.makeText(
                                        binding.root.context,
                                        "Permission Granted..",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                    Toast.makeText(
                                        binding.root.context,
                                        "Started generating PDF file..",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    CoroutineScope(Dispatchers.Main).launch {
                                        PdfGeneratorHelper(
                                            binding.root.context,
                                            getShoppingListItems()
                                        )
                                    }
                                }
                                permissionsAbstraction.removeListener()
                            }

                        })

                        startActivity(
                            Intent(
                                Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                                uri
                            )
                        )
                    }
                }

                checkForRuntimePermissions(grantResults).and(
                    Environment.isExternalStorageManager()
                ) -> {
                    Toast.makeText(
                        binding.root.context, "Started generating PDF file..",
                        Toast.LENGTH_SHORT
                    ).show()
                    CoroutineScope(Dispatchers.Main).launch {
                        PdfGeneratorHelper(
                            binding.root.context,
                            getShoppingListItems()
                        )
                    }
                }

                else -> Toast.makeText(
                    binding.root.context, "Permission Denied..",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun checkForRuntimePermissions(grantResults: IntArray) =
        (grantResults[0] == PackageManager.PERMISSION_GRANTED).and(
            grantResults[1]
                    == PackageManager.PERMISSION_GRANTED
        )
}
