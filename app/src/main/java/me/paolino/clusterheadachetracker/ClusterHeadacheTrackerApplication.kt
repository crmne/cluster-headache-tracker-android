package me.paolino.clusterheadachetracker

import android.app.Application
import android.webkit.CookieManager
import android.webkit.WebView
import dev.hotwire.core.bridge.BridgeComponentFactory
import dev.hotwire.core.bridge.KotlinXJsonConverter
import dev.hotwire.core.config.Hotwire
import dev.hotwire.core.turbo.config.PathConfiguration
import dev.hotwire.navigation.config.defaultFragmentDestination
import dev.hotwire.navigation.config.registerBridgeComponents
import dev.hotwire.navigation.config.registerFragmentDestinations
import dev.hotwire.navigation.fragments.HotwireWebBottomSheetFragment
import me.paolino.clusterheadachetracker.bridge.ButtonComponent
import me.paolino.clusterheadachetracker.bridge.ShareComponent
import me.paolino.clusterheadachetracker.fragments.WebFragment
import me.paolino.clusterheadachetracker.fragments.WebModalFragment

class ClusterHeadacheTrackerApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Enable WebView debugging in debug builds
        if (BuildConfig.DEBUG) {
            WebView.setWebContentsDebuggingEnabled(true)
            Hotwire.config.debugLoggingEnabled = true
        }

        // Configure cookies globally
        configureCookies()

        // Configure Hotwire
        configureHotwire()
    }

    private fun configureHotwire() {
        // Set custom user agent
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        val appVersion = packageInfo.versionName
        val buildNumber = packageInfo.versionCode
        Hotwire.config.applicationUserAgentPrefix = "Turbo Native; ClusterHeadacheTracker/$appVersion.$buildNumber;"

        // Configure JSON converter for bridge components
        Hotwire.config.jsonConverter = KotlinXJsonConverter()

        // Load path configuration
        Hotwire.loadPathConfiguration(
            context = this,
            location = PathConfiguration.Location(
                assetFilePath = "json/path-configuration.json",
                remoteFileUrl = "${AppConfig.baseUrl}/configurations/android_v2.json",
            ),
        )

        // Set the default fragment destination
        Hotwire.defaultFragmentDestination = WebFragment::class

        // Register fragment destinations
        Hotwire.registerFragmentDestinations(
            WebFragment::class,
            WebModalFragment::class,
            HotwireWebBottomSheetFragment::class,
        )

        // Register bridge components
        Hotwire.registerBridgeComponents(
            BridgeComponentFactory("button", ::ButtonComponent),
            BridgeComponentFactory("share", ::ShareComponent),
        )
    }

    private fun configureCookies() {
        // Enable cookies globally
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)

        // Sync cookies to ensure they persist
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.flush()
        } else {
            @Suppress("DEPRECATION")
            android.webkit.CookieSyncManager.createInstance(this)
        }
    }
}
