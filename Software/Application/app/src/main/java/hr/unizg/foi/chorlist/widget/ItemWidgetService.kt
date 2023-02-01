package hr.unizg.foi.chorlist.widget

import android.content.Intent
import android.util.Log
import android.widget.RemoteViewsService

/**
 * Extending RemoteViewsService class and declaring lazy property named container which gets
 * itemServiceContainer from applicationContext.
 *
 */
class ItemWidgetService : RemoteViewsService() {
    private val container by lazy { (applicationContext as WidgetController).itemServiceContainer }

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return ItemRemoteViewsFactory(applicationContext, container.repository)
    }
}