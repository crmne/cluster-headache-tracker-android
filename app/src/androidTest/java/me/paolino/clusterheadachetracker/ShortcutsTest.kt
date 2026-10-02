package me.paolino.clusterheadachetracker

import androidx.core.content.pm.ShortcutManagerCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShortcutsTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Test
    fun staticShortcutsDeepLinkToTheContractPaths() {
        val shortcuts = ShortcutManagerCompat.getShortcuts(context, ShortcutManagerCompat.FLAG_MATCH_MANIFEST)
            .associateBy { it.id }

        assertEquals(
            "${AppConfig.BASE_URL}/headache_logs/new?quick=1",
            DeepLinks.urlFrom(shortcuts.getValue("log_attack").intent),
        )
        assertEquals(
            "${AppConfig.BASE_URL}/current_attack",
            DeepLinks.urlFrom(shortcuts.getValue("current_attack").intent),
        )
    }
}
