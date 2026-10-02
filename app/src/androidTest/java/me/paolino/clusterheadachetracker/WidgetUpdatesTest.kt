package me.paolino.clusterheadachetracker

import android.content.Context
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import me.paolino.clusterheadachetracker.widget.AppShortcuts
import me.paolino.clusterheadachetracker.widget.WidgetStatus
import me.paolino.clusterheadachetracker.widget.WidgetStatusStore
import me.paolino.clusterheadachetracker.widget.WidgetUpdates
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WidgetUpdatesTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @After
    fun tearDown() {
        WidgetUpdates.clear(context)
    }

    @Test
    fun ongoingAttackIsStoredAndAddsTheEndAttackShortcut() {
        val status = WidgetStatus(ongoing = true, startedAt = "2026-10-02T09:00:00Z", locale = "it")

        WidgetUpdates.publish(context, status)

        assertEquals(status, WidgetStatusStore(context).load()?.status)
        val shortcut = dynamicShortcuts().single { it.id == AppShortcuts.END_ATTACK_ID }
        assertEquals("Termina attacco", shortcut.shortLabel.toString())
        assertEquals("${AppConfig.BASE_URL}/current_attack", DeepLinks.urlFrom(shortcut.intent))
    }

    @Test
    fun endedAttackRemovesTheShortcut() {
        WidgetUpdates.publish(context, WidgetStatus(ongoing = true, startedAt = "2026-10-02T09:00:00Z"))
        assertTrue(dynamicShortcuts().any { it.id == AppShortcuts.END_ATTACK_ID })

        WidgetUpdates.publish(context, WidgetStatus(ongoing = false, attackFreeDays = 0))

        assertFalse(dynamicShortcuts().any { it.id == AppShortcuts.END_ATTACK_ID })
    }

    @Test
    fun signingOutForgetsTheStatus() {
        WidgetUpdates.publish(context, WidgetStatus(attackFreeDays = 3))

        WidgetUpdates.clear(context)

        assertNull(WidgetStatusStore(context).load())
    }

    private fun dynamicShortcuts() =
        ShortcutManagerCompat.getShortcuts(context, ShortcutManagerCompat.FLAG_MATCH_DYNAMIC)
}
