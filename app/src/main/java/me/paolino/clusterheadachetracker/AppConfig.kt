package me.paolino.clusterheadachetracker

object AppConfig {
    val baseUrl: String
        get() = BuildConfig.BASE_URL

    val remoteBaseUrl: String
        get() = BuildConfig.REMOTE_BASE_URL
}
