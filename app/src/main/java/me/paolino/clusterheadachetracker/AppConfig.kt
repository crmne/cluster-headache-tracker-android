package me.paolino.clusterheadachetracker

import android.os.Build

object AppConfig {
    const val REMOTE_URL = "https://clusterheadachetracker.com"

    // For physical devices on same network
    const val LOCAL_URL_DEVICE = "http://192.168.8.220:3000"

    // For Android emulator
    const val LOCAL_URL_EMULATOR = "http://10.0.2.2:3000"

    /**
     * Returns the appropriate base URL based on build configuration
     * In debug builds, uses local development server
     * In release builds, uses production server
     */
    val baseUrl: String
        get() {
            return if (BuildConfig.DEBUG) {
                // Check if running on emulator
                if (isEmulator()) {
                    LOCAL_URL_EMULATOR
                } else {
                    LOCAL_URL_DEVICE
                }
            } else {
                REMOTE_URL
            }
        }

    @Suppress("CyclomaticComplexMethod")
    private fun isEmulator(): Boolean = (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
        Build.FINGERPRINT.startsWith("generic") ||
        Build.FINGERPRINT.startsWith("unknown") ||
        Build.HARDWARE.contains("goldfish") ||
        Build.HARDWARE.contains("ranchu") ||
        Build.MODEL.contains("google_sdk") ||
        Build.MODEL.contains("Emulator") ||
        Build.MODEL.contains("Android SDK built for x86") ||
        Build.MANUFACTURER.contains("Genymotion") ||
        Build.PRODUCT.contains("sdk_google") ||
        Build.PRODUCT.contains("google_sdk") ||
        Build.PRODUCT.contains("sdk") ||
        Build.PRODUCT.contains("sdk_x86") ||
        Build.PRODUCT.contains("sdk_gphone64_arm64") ||
        Build.PRODUCT.contains("vbox86p") ||
        Build.PRODUCT.contains("emulator") ||
        Build.PRODUCT.contains("simulator")
}
