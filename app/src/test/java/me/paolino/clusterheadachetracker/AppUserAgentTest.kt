package me.paolino.clusterheadachetracker

import org.junit.Assert.assertEquals
import org.junit.Test

class AppUserAgentTest {
    @Test
    fun googlePlayInstallsSayTheyCameFromGooglePlay() {
        assertEquals(
            "ClusterHeadacheTracker; platform=android; version=2.2.0; build=20200; store=play;",
            AppUserAgent.prefix("2.2.0", 20200, "com.android.vending"),
        )
    }

    @Test
    fun otherInstallsLeaveTheStoreOut() {
        val expected = "ClusterHeadacheTracker; platform=android; version=2.2.0; build=20200;"

        assertEquals(expected, AppUserAgent.prefix("2.2.0", 20200, null))
        assertEquals(expected, AppUserAgent.prefix("2.2.0", 20200, "com.google.android.packageinstaller"))
    }
}
