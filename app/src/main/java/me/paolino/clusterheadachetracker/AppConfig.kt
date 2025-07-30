package me.paolino.clusterheadachetracker

object AppConfig {
    const val REMOTE_URL = "https://clusterheadachetracker.com"

    // For physical devices on same network
    const val LOCAL_URL_DEVICE = "http://192.168.8.220:3000"

    /**
     * Returns the appropriate base URL based on build configuration
     * In debug builds, uses local development server
     * In release builds, uses production server
     */
    val baseUrl: String
        get() {
            return if (BuildConfig.DEBUG) {
                LOCAL_URL_DEVICE
            } else {
                REMOTE_URL
            }
        }
}
