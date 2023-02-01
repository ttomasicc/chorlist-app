package hr.unizg.foi.chorlist.listeners

import androidx.appcompat.widget.SearchView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Custom QueryTextListener that delays the textChange timeout to 700ms. To manage the delay cycle
 * without suspending the main thread, it uses another coroutine that is responsible for timer
 * countdown.
 *
 * @property lifecycleScope lifecycle to which the delayed query text listener should be tied to
 */
abstract class DelayedOnQueryTextListener(
    private val lifecycleScope: CoroutineScope
) : SearchView.OnQueryTextListener,
    android.widget.SearchView.OnQueryTextListener {
    private lateinit var textChangeCountDownJob: Job

    override fun onQueryTextSubmit(query: String?) = false

    override fun onQueryTextChange(newText: String?): Boolean {
        if (::textChangeCountDownJob.isInitialized)
            textChangeCountDownJob.cancel()

        textChangeCountDownJob = lifecycleScope.launch {
            delay(700)
            onDelayedQueryTextChange(newText ?: "")
        }

        return false
    }

    abstract fun onDelayedQueryTextChange(text: String)
}