package hr.unizg.foi.chorlist.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import hr.unizg.foi.chorlist.*
import hr.unizg.foi.chorlist.activities.ItemsActivity

/**
 * Inheriting constructor to initialize AppWidgetProvider class which is providing
 * the logic to update App Widget.
 */
class ItemWidget : AppWidgetProvider() {

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
    }

    /**
     * Reading shoppingListId and shoppingListName from shared preferences and updating App Widget
     * with remote views.
     *
     * @param context represents state of passed object
     * @param appWidgetManager represents interface for interacting with App Widgets
     * @param appWidgetIds represents Ids of widgets if there is more than one widget
     */
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {

        val sharedPreferences =
            context.getSharedPreferences(WIDGET_SHARED_PREFERENCES, Context.MODE_PRIVATE)

        val shoppingListName: String? = sharedPreferences
            .getString(WIDGET_LIST_NAME, "No such list")

        val shoppingListId: Int = sharedPreferences.getInt(WIDGET_LIST_ID, -1)

        appWidgetIds.forEach { appWidgetId ->
            val intent = Intent(context, ItemWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId.toLong())
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }

            val pendingIntent = Intent(context, ItemsActivity::class.java)
                .putExtra(INTENT_SHOPPING_LIST_ID, shoppingListId.toLong())
                .putExtra(INTENT_SHOPPING_LIST_NAME, shoppingListName)
                .let {
                    PendingIntent.getActivity(
                        context,
                        0,
                        it,
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                }

            /**
             * Binding RemoteViews object to variable views, that will later update App Widget
             * with the specific App Widget Id (in this context appWidgetId).
             */
            val views = RemoteViews(context.packageName, R.layout.chorlist_widget).apply {
                setRemoteAdapter(R.id.lv_widget, intent)
                setEmptyView(R.id.lv_widget, R.id.tv_empty_view)
                setOnClickPendingIntent(R.id.btn_widget_add_item, pendingIntent)
                setTextViewText(R.id.tv_widget_shopping_list_name, shoppingListName)
            }
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }
}