package com.nobody.valurex.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object WidgetUpdater {
    fun updateMoneyWidget(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val manager = GlanceAppWidgetManager(context)
                manager.getGlanceIds(MoneyWidget::class.java).forEach { id ->
                    MoneyWidget().update(context, id)
                }
            } catch (_: Exception) {}
        }
    }

    fun updateWishlistWidget(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val manager = GlanceAppWidgetManager(context)
                manager.getGlanceIds(WishlistWidget::class.java).forEach { id ->
                    WishlistWidget().update(context, id)
                }
            } catch (_: Exception) {}
        }
    }
}
