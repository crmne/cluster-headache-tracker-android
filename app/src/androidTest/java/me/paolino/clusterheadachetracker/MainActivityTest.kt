package me.paolino.clusterheadachetracker

import android.view.View
import android.view.ViewGroup
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.material.button.MaterialButton
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    @Test
    fun showsTheTabsInTheFloatingToolbar() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val buttons = activity.findViewById<ViewGroup>(R.id.tab_buttons)
                val labels = (0 until buttons.childCount).map { buttons.getChildAt(it).contentDescription.toString() }
                val expected = listOf(
                    R.string.tab_logs,
                    R.string.tab_charts,
                    R.string.tab_account,
                    R.string.tab_feedback,
                )
                    .map(activity::getString)
                assertEquals(expected, labels)
                // Only the chosen tab says its name.
                assertEquals(
                    activity.getString(R.string.tab_logs),
                    (buttons.getChildAt(0) as MaterialButton).text.toString(),
                )
                assertEquals("", (buttons.getChildAt(1) as MaterialButton).text.toString())
            }
        }
    }

    @Test
    fun newLogOpensTheFormOverTheTabShowing() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<ViewGroup>(R.id.tab_buttons).getChildAt(1).performClick()
            }
            scenario.onActivity { activity -> activity.findViewById<View>(R.id.new_log).performClick() }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()

            scenario.onActivity { activity ->
                assertEquals(activity.getString(R.string.tab_charts), activity.getString(activity.selectedTab.title))
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
