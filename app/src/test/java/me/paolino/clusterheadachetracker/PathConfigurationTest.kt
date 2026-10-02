package me.paolino.clusterheadachetracker

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

/**
 * Applies the bundled path configuration the way Hotwire Native Android does: every matching rule is merged in
 * order (later rules win), and patterns are matched against the path plus query string.
 */
class PathConfigurationTest {
    private val rules = Json.parseToJsonElement(File("src/main/assets/json/path-configuration.json").readText())
        .jsonObject.getValue("rules").jsonArray.map { it.jsonObject }

    @Test
    fun logFormsAndTheCurrentAttackAreModalsWithoutPullToRefresh() {
        listOf(
            "/headache_logs/new",
            "/headache_logs/new?quick=1",
            "/headache_logs/12/edit",
            "/current_attack",
            "/medications/new",
            "/medications/3/edit",
            "/medication_doses/new",
            "/medication_doses/7/edit",
        )
            .forEach { path ->
                val properties = properties(path)
                assertEquals(path, "modal", properties["context"])
                assertEquals(path, "hotwire://fragment/web/modal", properties["uri"])
                assertEquals(path, "false", properties["pull_to_refresh_enabled"])
            }
    }

    @Test
    fun signInIsAModal() {
        assertEquals("modal", properties("/users/sign_in")["context"])
        assertEquals("false", properties("/users/sign_in")["pull_to_refresh_enabled"])
    }

    @Test
    fun tabRootsReplaceAndKeepPullToRefresh() {
        val properties = properties("/headache_logs")

        assertEquals("default", properties["context"])
        assertEquals("replace", properties["presentation"])
        assertEquals("true", properties["pull_to_refresh_enabled"])
    }

    @Test
    fun historicalLocationsAreHandledNatively() {
        assertEquals("refresh", properties("/refresh_historical_location")["presentation"])
        assertEquals("none", properties("/resume_historical_location")["presentation"])
    }

    @Test
    fun otherPagesArePushedWithPullToRefresh() {
        val properties = properties("/headache_logs/12")

        assertEquals("hotwire://fragment/web", properties["uri"])
        assertEquals("true", properties["pull_to_refresh_enabled"])
    }

    private fun properties(path: String): Map<String, String> = rules
        .filter { rule -> rule.patterns.any { Regex(it, RegexOption.IGNORE_CASE).containsMatchIn(path) } }
        .fold(mapOf()) { merged, rule -> merged + rule.getValue("properties").jsonObject.strings() }

    private val JsonObject.patterns get() = getValue("patterns").jsonArray.map { it.jsonPrimitive.content }

    private fun JsonObject.strings() = mapValues { (_, value: JsonElement) -> value.jsonPrimitive.content }
}
