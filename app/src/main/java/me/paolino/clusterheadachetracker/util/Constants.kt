package me.paolino.clusterheadachetracker.util

import me.paolino.clusterheadachetracker.AppConfig

val SIGN_IN_URL = "${AppConfig.baseUrl}/users/sign_in"
val SIGN_OUT_URL = "${AppConfig.baseUrl}/users/sign_out"
val NEW_HEADACHE_LOG_URL = "${AppConfig.baseUrl}/headache_logs/new"

object AuthEvents {
    const val SIGN_OUT_REQUESTED = "signOutRequested"
    const val AUTHENTICATION_CHANGED = "authenticationChanged"
}
