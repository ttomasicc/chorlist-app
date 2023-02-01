package hr.unizg.foi.chorlist.widget

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import hr.unizg.foi.chorlist.R
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Extending Application class and overriding onCreate method.
 */
class WidgetController : Application() {

    lateinit var itemServiceContainer: ItemServiceContainer

    /**
     * Initializing ItemServiceContainer and setting its flow to listen to the data property of
     * the repository object of the itemServiceContainer. Also notifying AppWidgetManager that the
     * view (lv_widget view) need to be updated.
     */
    override fun onCreate() {
        super.onCreate()

        itemServiceContainer = ItemServiceContainer(this)

        itemServiceContainer.repository.data
            .onEach {
                val component = ComponentName(this, ItemWidget::class.java)
                val ids = AppWidgetManager.getInstance(applicationContext).getAppWidgetIds(component)

                itemServiceContainer.appWidgetManager.notifyAppWidgetViewDataChanged(ids, R.id.lv_widget)
            }
            .launchIn(scope = itemServiceContainer.scope)
    }
}