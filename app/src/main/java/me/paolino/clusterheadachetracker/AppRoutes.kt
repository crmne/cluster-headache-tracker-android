package me.paolino.clusterheadachetracker

import androidx.core.net.toUri

object AppRoutes {
    const val QUICK_LOG_PATH = "/headache_logs/new?quick=1"
    const val CURRENT_ATTACK_PATH = "/current_attack"

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
        val path = location?.let { runCatching { it.toUri().path }.getOrNull() }
        return path in authenticationPaths
    }

    /**
     * Resolves an app-relative path (from a shortcut, widget or other deep link) against the
     * configured server. Anything that isn't a plain relative path is rejected so a crafted
     * intent can never point the WebView at another host.
     */
    fun urlForPath(path: String?): String? = if (isAppPath(path)) "${AppConfig.BASE_URL}$path" else null

    fun isAppPath(path: String?): Boolean = path != null &&
        path.startsWith("/") &&
        !path.startsWith("//") &&
        path.none { it.isWhitespace() || it == '\\' || it == '@' }
}
