package me.paolino.clusterheadachetracker.widget

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.json.Json
import java.time.Instant

/** Shared storage for the latest widget status, read by the home-screen widget and dynamic shortcuts. */
class WidgetStatusStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun save(status: WidgetStatus, receivedAt: Instant = Instant.now()) {
        preferences.edit {
            putString(KEY_STATUS, json.encodeToString(WidgetStatus.serializer(), status))
            putLong(KEY_RECEIVED_AT, receivedAt.toEpochMilli())
        }
    }

    fun load(): WidgetSnapshot? {
        val encoded = preferences.getString(KEY_STATUS, null) ?: return null
        val status =
            runCatching { json.decodeFromString(WidgetStatus.serializer(), encoded) }.getOrNull() ?: return null
        return WidgetSnapshot(status, Instant.ofEpochMilli(preferences.getLong(KEY_RECEIVED_AT, 0)))
    }

    /** Emits the stored snapshot whenever it changes, so a running widget session recomposes. */
    fun snapshots(): Flow<WidgetSnapshot?> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> trySend(load()) }
        preferences.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    fun clear() {
        preferences.edit { clear() }
    }

    companion object {
        private const val PREFERENCES_NAME = "widget_status"
        private const val KEY_STATUS = "status"
        private const val KEY_RECEIVED_AT = "received_at"

        val json = Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
    }
}
