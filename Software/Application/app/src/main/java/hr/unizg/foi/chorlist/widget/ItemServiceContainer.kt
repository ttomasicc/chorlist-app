package hr.unizg.foi.chorlist.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Creates CoroutineScope with job and its dispatcher which will be passed to ItemWidgetRepository
 * class. Class also creates instance of AppWidgetManager class and passing it current context.
 *
 * @param context represents state of passed object
 */
class ItemServiceContainer(context: Context) {

    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val repository = ItemWidgetRepository(scope, context)

    val appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(context)
}