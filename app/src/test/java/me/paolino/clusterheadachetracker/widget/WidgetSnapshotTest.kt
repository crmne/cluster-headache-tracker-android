package me.paolino.clusterheadachetracker.widget

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

class WidgetSnapshotTest {
    private val zone = ZoneId.of("Europe/Berlin")
    private val receivedAt = Instant.parse("2026-10-02T08:00:00Z")

    @Test
    fun decodesTheContractPayloadIgnoringUnknownKeys() {
        val json = """
            {"ongoing":true,"startedAt":"2026-10-02T09:15:00+02:00","lastAttackAt":null,
             "attackFreeDays":0,"attacksToday":2,"locale":"de","extra":"ignored"}
        """.trimIndent()

        val status = WidgetStatusStore.json.decodeFromString(WidgetStatus.serializer(), json)

        assertEquals(
            WidgetStatus(
                ongoing = true,
                startedAt = "2026-10-02T09:15:00+02:00",
                attacksToday = 2,
                locale = "de",
            ),
            status,
        )
    }

    @Test
    fun decodesMissingFieldsWithDefaults() {
        val status = WidgetStatusStore.json.decodeFromString(WidgetStatus.serializer(), "{}")

        assertEquals(WidgetStatus(), status)
    }

    @Test
    fun ongoingAttackReportsElapsedTimeFromItsStart() {
        val snapshot = snapshot(WidgetStatus(ongoing = true, startedAt = "2026-10-02T10:00:00+02:00"))

        assertTrue(snapshot.ongoing)
        assertEquals(Duration.ofMinutes(25), snapshot.elapsed(Instant.parse("2026-10-02T08:25:00Z")))
    }

    @Test
    fun elapsedTimeNeverGoesNegative() {
        val snapshot = snapshot(WidgetStatus(ongoing = true, startedAt = "2026-10-02T10:00:00Z"))

        assertEquals(Duration.ZERO, snapshot.elapsed(Instant.parse("2026-10-02T09:00:00Z")))
    }

    @Test
    fun ongoingWithoutAStartTimeIsNotTreatedAsOngoing() {
        assertFalse(snapshot(WidgetStatus(ongoing = true, startedAt = null)).ongoing)
        assertFalse(snapshot(WidgetStatus(ongoing = true, startedAt = "not a date")).ongoing)
    }

    @Test
    fun parsesFractionalSecondsAndUtc() {
        assertEquals(
            Instant.parse("2026-10-02T07:59:59.123Z"),
            WidgetSnapshot.parseInstant("2026-10-02T07:59:59.123Z"),
        )
        assertNull(WidgetSnapshot.parseInstant(null))
    }

    @Test
    fun attackFreeDaysAdvanceWithTheCalendarWithoutTheServer() {
        val snapshot = snapshot(WidgetStatus(attackFreeDays = 4, lastAttackAt = "2026-09-28T03:00:00+02:00"))

        assertEquals(4, snapshot.attackFreeDays(Instant.parse("2026-10-02T21:00:00Z"), zone))
        assertEquals(5, snapshot.attackFreeDays(Instant.parse("2026-10-02T22:30:00Z"), zone))
        assertEquals(7, snapshot.attackFreeDays(Instant.parse("2026-10-05T12:00:00Z"), zone))
    }

    @Test
    fun attackFreeDaysAreZeroDuringAnAttack() {
        val snapshot = snapshot(
            WidgetStatus(ongoing = true, startedAt = "2026-10-02T09:00:00Z", attackFreeDays = 3),
        )

        assertEquals(0, snapshot.attackFreeDays(Instant.parse("2026-10-03T12:00:00Z"), zone))
    }

    @Test
    fun attacksTodayOnlyCountOnTheDayTheyWereReported() {
        val snapshot = snapshot(WidgetStatus(attacksToday = 3))

        assertEquals(3, snapshot.attacksToday(Instant.parse("2026-10-02T21:59:00Z"), zone))
        assertEquals(0, snapshot.attacksToday(Instant.parse("2026-10-02T22:01:00Z"), zone))
    }

    @Test
    fun onlySupportedLocalesAreUsed() {
        assertEquals("it", snapshot(WidgetStatus(locale = "it")).locale)
        assertNull(snapshot(WidgetStatus(locale = "fr")).locale)
        assertNull(snapshot(WidgetStatus(locale = null)).locale)
    }

    private fun snapshot(status: WidgetStatus) = WidgetSnapshot(status, receivedAt)
}
