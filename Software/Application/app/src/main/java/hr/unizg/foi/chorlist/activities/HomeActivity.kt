package hr.unizg.foi.chorlist.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.lifecycle.lifecycleScope
import hr.unizg.foi.chorlist.R
import hr.unizg.foi.chorlist.SEARCH_ITEMS_FRAGMENT_TAG
import hr.unizg.foi.chorlist.databinding.ActivityHomeBinding
import hr.unizg.foi.chorlist.fragments.SearchItemsFragment
import hr.unizg.foi.chorlist.listeners.DelayedOnQueryTextListener

/**
 * Responsible for managing home screen fragments and main application toolbar.
 */
class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding

    /**
     * Binds the logic to the layout.
     *
     * @param savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
    }

    /**
     * Inflates and configures the [Menu] functionality.
     *
     * @param menu application menu
     * @return true if the configuration ended successfully, otherwise false
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        setFullWidthSearch(menu)

        val searchView = menu.findItem(R.id.action_search).actionView as SearchView
        configureSearchView(searchView)

        return super.onCreateOptionsMenu(menu)
    }

    /**
     * Adds functionality to each of the [MenuItem] from the menu.
     *
     * @param item the captured [MenuItem]
     * @return true if the configuration ended successfully, otherwise false
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.action_search -> {
                supportFragmentManager.commit {
                    setReorderingAllowed(true)
                    addToBackStack(null)
                    replace<SearchItemsFragment>(binding.fcvMain.id, SEARCH_ITEMS_FRAGMENT_TAG)
                }
                true
            }
            R.id.action_user_profile -> {
                startActivity(Intent(this, UserProfileActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    /**
     * Sets the full width search bar on the given [menu]
     *
     * @param menu the [Menu] which should incorporate the full width search bar
     */
    private fun setFullWidthSearch(menu: Menu) {
        val searchActionMenuItem = menu.findItem(R.id.action_search)
        val userProfileActionMenuItem = menu.findItem(R.id.action_user_profile)

        searchActionMenuItem.setOnActionExpandListener(object :
            MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                userProfileActionMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                supportFragmentManager.popBackStack()
                invalidateOptionsMenu()
                return true
            }
        })
    }

    /**
     * Configures the [SearchView] object so that it enables delayed processing of the text changed,
     * while also sending it to the [SearchItemsFragment] to process the search.
     *
     * @param searchView [SearchView] object that should incorporate the delayed text search
     * processing
     * @return the configured [SearchView] object that incorporates the delayed text search
     * processing
     */
    private fun configureSearchView(searchView: SearchView) =
        searchView.apply {
            queryHint = context.getString(R.string.search_items)
            setOnQueryTextListener(object : DelayedOnQueryTextListener(lifecycleScope) {
                override fun onDelayedQueryTextChange(text: String) {
                    val fragment =
                        supportFragmentManager.findFragmentByTag(SEARCH_ITEMS_FRAGMENT_TAG)
                    if (fragment is SearchItemsFragment)
                        fragment.search(text)
                }
            })
        }
}