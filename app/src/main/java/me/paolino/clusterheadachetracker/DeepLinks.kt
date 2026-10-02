package me.paolino.clusterheadachetracker

import android.content.Context
import android.content.Intent

/**
 * Deep links used by home-screen shortcuts and the widget. They carry an app-relative path
 * that [MainActivity] routes on the current tab once its navigator is ready.
 */
object DeepLinks {
    const val ACTION_OPEN_PATH = "me.paolino.clusterheadachetracker.action.OPEN_PATH"
    const val EXTRA_PATH = "path"

    fun intent(context: Context, path: String): Intent = Intent(context, MainActivity::class.java)
        .setAction(ACTION_OPEN_PATH)
        .putExtra(EXTRA_PATH, path)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)

    fun urlFrom(intent: Intent?): String? {
        if (intent?.action != ACTION_OPEN_PATH) return null
        return AppRoutes.urlForPath(intent.getStringExtra(EXTRA_PATH))
    }
}
