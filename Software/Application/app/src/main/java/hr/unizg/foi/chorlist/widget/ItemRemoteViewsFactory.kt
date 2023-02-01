package hr.unizg.foi.chorlist.widget

import android.content.Context
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import hr.unizg.foi.chorlist.R

/**
 * Implements methods of RemoteViewsFactory abstract class that is used to create views for an
 * App Widget
 *
 * @property context represents state of passed object
 * @property repository represents ItemWidgetRepository class
 */
class ItemRemoteViewsFactory(
    private val context: Context,
    private val repository : ItemWidgetRepository
) : RemoteViewsService.RemoteViewsFactory {

    data class WidgetItem(val text: String)

    private var widgetItems: List<WidgetItem> = emptyList()

    override fun onCreate() {
    }

    override fun onDestroy() {
    }

    override fun onDataSetChanged() {
        widgetItems = repository.data.value.map { WidgetItem(it.name) }
    }

    override fun getCount(): Int {
        return widgetItems.size
    }

    /**
     * Returns RemoteViews object that will display views cointained in widget_item layout file
     *
     * @param position where position is position of widgetItems id in List
     * @return RemoteViews object
     */
    override fun getViewAt(position: Int): RemoteViews {
        return RemoteViews(context.packageName, R.layout.widget_item).apply {
            setTextViewText(R.id.tv_item, widgetItems[position].text)
        }
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(position: Int): Long {
        return widgetItems[position].hashCode().toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }
}