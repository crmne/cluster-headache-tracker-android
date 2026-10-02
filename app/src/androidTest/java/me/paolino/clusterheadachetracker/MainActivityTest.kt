package me.paolino.clusterheadachetracker

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    @Test
    fun showsTheNativeTabBar() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val menu = activity.findViewById<BottomNavigationView>(R.id.bottom_nav).menu
                val titles = (0 until menu.size()).map { menu.getItem(it).title.toString() }
                val expected = listOf(
                    R.string.tab_logs,
                    R.string.tab_charts,
                    R.string.tab_new,
                    R.string.tab_account,
                    R.string.tab_feedback,
                )
                    .map(activity::getString)
                assertEquals(expected, titles)
            }
        }
    }

    @Test
    fun newTabOpensTheLogFormWithoutStayingSelected() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity -> activity.bottomNav().selectedItemId = MainTabs.CHARTS_INDEX }
            scenario.onActivity { activity -> activity.bottomNav().selectedItemId = MainTabs.NEW_INDEX }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()

            scenario.onActivity { activity ->
                assertEquals(MainTabs.CHARTS_INDEX, activity.bottomNav().selectedItemId)
            }
        }
    }

    @Test
    fun shortcutIntentRoutesToTheCurrentAttack() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent = DeepLinks.intent(context, AppRoutes.CURRENT_ATTACK_PATH)

        ActivityScenario.launch<MainActivity>(intent).use { scenario ->
            waitForDeepLink(scenario, "${AppConfig.BASE_URL}/current_attack")
        }
    }

    @Test
    fun quickLogIntentOpensTheQuickLogForm() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent = DeepLinks.intent(context, AppRoutes.QUICK_LOG_PATH)

        ActivityScenario.launch<MainActivity>(intent).use { scenario ->
            waitForDeepLink(scenario, "${AppConfig.BASE_URL}/headache_logs/new?quick=1")
        }
    }

    private fun MainActivity.bottomNav() = findViewById<BottomNavigationView>(R.id.bottom_nav)

    private fun waitForDeepLink(scenario: ActivityScenario<MainActivity>, url: String) {
        val deadline = System.currentTimeMillis() + TIMEOUT_MS
        var routed: String? = null
        while (System.currentTimeMillis() < deadline && routed != url) {
            scenario.onActivity { routed = it.routedDeepLinkUrl }
            if (routed != url) Thread.sleep(POLL_MS)
        }
        assertEquals(url, routed)
    }

    private companion object {
        const val TIMEOUT_MS = 15_000L
        const val POLL_MS = 200L
    }
}
