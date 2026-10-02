package me.paolino.clusterheadachetracker

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import me.paolino.clusterheadachetracker.widget.AttackWidgetReceiver
import me.paolino.clusterheadachetracker.widget.WidgetStatus
import me.paolino.clusterheadachetracker.widget.WidgetUpdates
import org.junit.Assert.assertNotNull
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Pins the widget to the launcher (accepting the system dialog with UI Automator), then checks that it follows
 * the status the web app reports and that its button opens the app.
 */
@RunWith(AndroidJUnit4::class)
class HomeScreenWidgetTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    private val widgets = AppWidgetManager.getInstance(context)
    private val provider = ComponentName(context, AttackWidgetReceiver::class.java)

    @Before
    fun pinWidget() {
        assumeTrue(widgets.isRequestPinAppWidgetSupported)
        device.pressHome()
        if (widgets.getAppWidgetIds(provider).isNotEmpty()) return

        // Only a foreground app may ask the launcher to pin a widget.
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { widgets.requestPinAppWidget(provider, null, null) }
            device.wait(Until.findObject(By.text(ADD_BUTTON)), TIMEOUT_MS)?.click()
                ?: device.dumpWindowHierarchy(java.io.File(context.cacheDir, "pin.xml"))
            device.wait({ widgets.getAppWidgetIds(provider).isNotEmpty() }, TIMEOUT_MS)
        }
        device.pressHome()
    }

    @Test
    fun showsTheOngoingAttackAndOpensIt() {
        val startedAt = Instant.now().minus(12, ChronoUnit.MINUTES).toString()
        WidgetUpdates.publish(context, WidgetStatus(ongoing = true, startedAt = startedAt, attacksToday = 1))

        assertNotNull(findOnHomeScreen("Attack ongoing"))
        device.findObject(By.text("End attack")).click()

        assertNotNull(device.wait(Until.hasObject(By.pkg(context.packageName).depth(0)), TIMEOUT_MS))
    }

    @Test
    fun showsDaysAttackFreeInTheUsersLanguage() {
        WidgetUpdates.publish(
            context,
            WidgetStatus(attackFreeDays = 4, lastAttackAt = "2026-09-28T03:00:00Z", locale = "de"),
        )

        assertNotNull(findOnHomeScreen("Tage attackenfrei"))
        assertNotNull(device.findObject(By.text("Attacke erfassen")))
    }

    /** The launcher may put a pinned widget on any home screen page, so look through them. */
    private fun findOnHomeScreen(text: String): UiObject2? {
        device.pressHome()
        repeat(HOME_PAGES) {
            device.wait(Until.findObject(By.text(text)), PAGE_TIMEOUT_MS)?.let { return it }
            device.swipe(
                device.displayWidth * 9 / 10,
                device.displayHeight / 2,
                device.displayWidth / 10,
                device.displayHeight / 2,
                SWIPE_STEPS,
            )
        }
        return null
    }

    private companion object {
        const val HOME_PAGES = 4
        const val PAGE_TIMEOUT_MS = 3_000L
        const val SWIPE_STEPS = 20
        const val ADD_BUTTON = "Add to home screen"
        const val TIMEOUT_MS = 10_000L
    }
}
