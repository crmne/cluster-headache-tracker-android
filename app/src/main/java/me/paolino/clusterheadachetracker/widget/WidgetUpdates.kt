package me.paolino.clusterheadachetracker.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Pushes a new widget status to shared storage, the home-screen widget and the dynamic shortcuts. */
object WidgetUpdates {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun publish(context: Context, status: WidgetStatus) {
        val appContext = context.applicationContext
        WidgetStatusStore(appContext).save(status)
        refresh(appContext)
    }

    fun clear(context: Context) {
        val appContext = context.applicationContext
        WidgetStatusStore(appContext).clear()
        refresh(appContext)
    }

    private fun refresh(context: Context) {
        AppShortcuts.update(context, WidgetStatusStore(context).load())
        scope.launch { AttackWidget().updateAll(context) }
    }
}
