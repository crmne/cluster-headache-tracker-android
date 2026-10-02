package me.paolino.clusterheadachetracker

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeepLinksTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Test
    fun opensAppPaths() {
        val intent = DeepLinks.intent(context, AppRoutes.CURRENT_ATTACK_PATH)

        assertEquals("${AppConfig.BASE_URL}/current_attack", DeepLinks.urlFrom(intent))
    }

    @Test
    fun ignoresOtherIntentsAndForeignUrls() {
        assertNull(DeepLinks.urlFrom(Intent(Intent.ACTION_MAIN)))
        assertNull(DeepLinks.urlFrom(DeepLinks.intent(context, "https://evil.example/")))
        assertNull(DeepLinks.urlFrom(null))
    }
}
