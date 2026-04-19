package me.paolino.clusterheadachetracker

import android.net.Uri

object AppRoutes {
    val logsUrl = "${AppConfig.BASE_URL}/headache_logs"
    val chartsUrl = "${AppConfig.BASE_URL}/charts"
    val newHeadacheLogUrl = "${AppConfig.BASE_URL}/headache_logs/new"
    val accountUrl = "${AppConfig.BASE_URL}/settings"
    val feedbackUrl = "${AppConfig.BASE_URL}/feedback"
    val signInUrl = "${AppConfig.BASE_URL}/users/sign_in"

    private val authenticationPaths = setOf(
        "/users/sign_in",
        "/users/sign_up",
        "/users/password",
        "/users/password/new",
        "/users/password/edit",
        "/login",
        "/signup",
        "/register",
    )

    fun isAuthenticationLocation(location: String?): Boolean {
        val path = location?.let { runCatching { Uri.parse(it).path }.getOrNull() }
        return path in authenticationPaths
    }
}
