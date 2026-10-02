package me.paolino.clusterheadachetracker.widget

import kotlinx.serialization.Serializable
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * Payload of the `widget-status` bridge component's `connect` event, as fixed by the web/native contract:
 * `{ ongoing, startedAt, lastAttackAt, attackFreeDays, attacksToday, locale }`.
 */
@Serializable
data class WidgetStatus(
    val ongoing: Boolean = false,
    val startedAt: String? = null,
    val lastAttackAt: String? = null,
    val attackFreeDays: Int = 0,
    val attacksToday: Int = 0,
    val locale: String? = null,
)

/**
 * What the widget and shortcuts show, derived from the last [WidgetStatus] the web app sent and when it
 * arrived. Widgets never call the server, so counts that depend on the date are advanced locally.
 */
data class WidgetSnapshot(val status: WidgetStatus, val receivedAt: Instant) {
    val ongoing: Boolean get() = status.ongoing && startedAt != null

    val startedAt: Instant? get() = parseInstant(status.startedAt)

    val lastAttackAt: Instant? get() = parseInstant(status.lastAttackAt)

    val locale: String? get() = status.locale?.takeIf { it in SUPPORTED_LOCALES }

    fun elapsed(now: Instant): Duration? = startedAt?.let { Duration.between(it, now).coerceAtLeast(Duration.ZERO) }

    fun attackFreeDays(now: Instant, zone: ZoneId): Int {
        if (ongoing) return 0
        val daysSinceReceived = ChronoUnit.DAYS.between(localDate(receivedAt, zone), localDate(now, zone))
        return (status.attackFreeDays + daysSinceReceived.coerceAtLeast(0)).toInt()
    }

    fun attacksToday(now: Instant, zone: ZoneId): Int =
        if (localDate(receivedAt, zone) == localDate(now, zone)) status.attacksToday else 0

    companion object {
        val SUPPORTED_LOCALES = setOf("en", "de", "it", "es")

        fun parseInstant(value: String?): Instant? =
            value?.let { runCatching { OffsetDateTime.parse(it).toInstant() }.getOrNull() }

        private fun localDate(instant: Instant, zone: ZoneId): LocalDate = instant.atZone(zone).toLocalDate()
    }
}
