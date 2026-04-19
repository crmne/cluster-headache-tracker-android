package me.paolino.clusterheadachetracker

import android.net.Uri

object AppRoutes {
    private const val recedeHistoricalPath = "/recede_historical_location"

    private val authenticationPaths = setOf(
        "/users/sign_in",
        "/users/sign_up",
        "/users/password",
        "/users/password/new",
        "/users/password/edit",
        "/login",
        "/signup",
        "/register",
        "/sessions/new",
        "/session",
    )

    val logsUrl: String = "${AppConfig.baseUrl}/headache_logs"
    val chartsUrl: String = "${AppConfig.baseUrl}/charts"
    val newHeadacheLogUrl: String = "${AppConfig.baseUrl}/headache_logs/new"
    val accountUrl: String = "${AppConfig.baseUrl}/settings"
    val feedbackUrl: String = "${AppConfig.baseUrl}/feedback"
    val signInUrl: String = "${AppConfig.baseUrl}/users/sign_in"

    fun isAuthenticationLocation(location: String?): Boolean {
        val path = location?.path() ?: return false
        return path in authenticationPaths
    }

    fun didCompleteAuthentication(location: String, previousLocation: String?): Boolean {
        val path = location.path()
        return path == recedeHistoricalPath ||
            (isAuthenticationLocation(previousLocation) && !isAuthenticationLocation(location))
    }

    private fun String.path(): String? = runCatching { Uri.parse(this).path }.getOrNull()
}
