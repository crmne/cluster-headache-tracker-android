package me.paolino.clusterheadachetracker

import android.app.Application
import android.webkit.CookieManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.pm.PackageInfoCompat
import com.google.android.material.color.DynamicColors
import com.masilotti.bridgecomponents.shared.Bridgework
import dev.hotwire.core.bridge.KotlinXJsonConverter
import dev.hotwire.core.config.Hotwire
import dev.hotwire.core.logging.HotwireLogLevel
import dev.hotwire.core.turbo.config.PathConfiguration
import dev.hotwire.navigation.config.defaultFragmentDestination
import dev.hotwire.navigation.config.registerBridgeComponents
import dev.hotwire.navigation.config.registerFragmentDestinations
import dev.hotwire.navigation.config.registerRouteDecisionHandlers
import dev.hotwire.navigation.routing.AppNavigationRouteDecisionHandler
import dev.hotwire.navigation.routing.BrowserTabRouteDecisionHandler
import dev.hotwire.navigation.routing.SystemNavigationRouteDecisionHandler
import me.paolino.clusterheadachetracker.bridge.AppButtonComponent
import me.paolino.clusterheadachetracker.bridge.DownloadComponent
import me.paolino.clusterheadachetracker.bridge.WidgetStatusComponent
import me.paolino.clusterheadachetracker.downloads.DownloadRouteDecisionHandler
import me.paolino.clusterheadachetracker.fragments.WebFragment
import me.paolino.clusterheadachetracker.fragments.WebModalFragment

class ClusterHeadacheTrackerApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Start out explicitly following the system so the theme bridge component's `{theme: null}` is a no-op
        // instead of a night-mode change that recreates the activity.
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        DynamicColors.applyToActivitiesIfAvailable(this)
        CookieManager.getInstance().setAcceptCookie(true)
        configureHotwire()
    }

    private fun configureHotwire() {
        Hotwire.config.logger.logLevel = if (BuildConfig.DEBUG) HotwireLogLevel.DEBUG else HotwireLogLevel.NONE
        Hotwire.config.webViewDebuggingEnabled = BuildConfig.DEBUG
        Hotwire.config.jsonConverter = KotlinXJsonConverter()
        Hotwire.config.applicationUserAgentPrefix = buildUserAgentPrefix()

        Hotwire.loadPathConfiguration(
            context = this,
            location = PathConfiguration.Location(
                assetFilePath = "json/path-configuration.json",
                remoteFileUrl = "${AppConfig.BASE_URL}/configurations/android_v2.json",
            ),
        )

        Hotwire.defaultFragmentDestination = WebFragment::class
        Hotwire.registerFragmentDestinations(
            WebFragment::class,
            WebModalFragment::class,
        )
        Hotwire.registerRouteDecisionHandlers(
            DownloadRouteDecisionHandler(),
            AppNavigationRouteDecisionHandler(),
            BrowserTabRouteDecisionHandler(),
            SystemNavigationRouteDecisionHandler(),
        )
        @Suppress("SpreadOperator")
        Hotwire.registerBridgeComponents(*bridgeComponents.toTypedArray())
    }

    private fun buildUserAgentPrefix(): String {
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        val versionName = packageInfo.versionName
        val versionCode = PackageInfoCompat.getLongVersionCode(packageInfo)
        return "ClusterHeadacheTracker; platform=android; version=$versionName; build=$versionCode;"
    }

    companion object {
        /**
         * Joe Masilotti's core components (alert, form, haptic, menu, review-prompt, search, share, theme, toast),
         * our own `button` (native print and sign-out), `download` for PDF reports, and `widget-status` for the
         * home-screen widget and shortcuts.
         */
        val bridgeComponents = Bridgework.coreComponents
            .filter { it.name != "button" }
            .plus(AppButtonComponent.factory)
            .plus(DownloadComponent.factory)
            .plus(WidgetStatusComponent.factory)
    }
}
