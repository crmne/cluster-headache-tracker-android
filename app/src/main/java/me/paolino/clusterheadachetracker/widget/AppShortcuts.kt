package me.paolino.clusterheadachetracker.widget

import android.content.Context
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import me.paolino.clusterheadachetracker.AppRoutes
import me.paolino.clusterheadachetracker.DeepLinks
import me.paolino.clusterheadachetracker.R

/**
 * The static shortcuts ("Log attack", "Current attack") live in res/xml/shortcuts.xml. While an attack is
 * ongoing we add one dynamic shortcut that goes straight to ending it.
 */
object AppShortcuts {
    const val END_ATTACK_ID = "end_attack"

    fun update(context: Context, snapshot: WidgetSnapshot?) {
        if (snapshot?.ongoing == true) {
            val localized = context.localized(snapshot.locale)
            val shortcut = ShortcutInfoCompat.Builder(context, END_ATTACK_ID)
                .setShortLabel(localized.getString(R.string.shortcut_end_attack_short))
                .setLongLabel(localized.getString(R.string.shortcut_end_attack_long))
                .setIcon(IconCompat.createWithResource(context, R.drawable.ic_shortcut_end_attack))
                .setIntent(DeepLinks.intent(context, AppRoutes.CURRENT_ATTACK_PATH))
                .setRank(0)
                .build()
            ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)
        } else {
            ShortcutManagerCompat.removeDynamicShortcuts(context, listOf(END_ATTACK_ID))
        }
    }
}
