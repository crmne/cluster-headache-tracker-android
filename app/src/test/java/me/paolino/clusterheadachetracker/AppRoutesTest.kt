package me.paolino.clusterheadachetracker

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppRoutesTest {
    @Test
    fun resolvesContractDeepLinksAgainstTheConfiguredServer() {
        assertEquals("${AppConfig.BASE_URL}/headache_logs/new?quick=1", AppRoutes.urlForPath(AppRoutes.QUICK_LOG_PATH))
        assertEquals("${AppConfig.BASE_URL}/current_attack", AppRoutes.urlForPath(AppRoutes.CURRENT_ATTACK_PATH))
    }

    @Test
    fun rejectsAnythingThatCouldLeaveTheApp() {
        listOf(
            null,
            "",
            "current_attack",
            "//evil.example/path",
            "https://evil.example/",
            "/\\evil.example",
            "/@evil.example",
            "/current attack",
        ).forEach { assertNull("$it should be rejected", AppRoutes.urlForPath(it)) }
    }
}
