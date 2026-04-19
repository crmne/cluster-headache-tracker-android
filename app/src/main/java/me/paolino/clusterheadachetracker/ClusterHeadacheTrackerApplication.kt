package me.paolino.clusterheadachetracker

import android.app.Application
import android.webkit.CookieManager
import androidx.core.content.pm.PackageInfoCompat
import com.masilotti.bridgecomponents.shared.Bridgework
import dev.hotwire.core.bridge.BridgeComponent
import dev.hotwire.core.bridge.BridgeComponentFactory
import dev.hotwire.core.bridge.KotlinXJsonConverter
import dev.hotwire.core.config.Hotwire
import dev.hotwire.core.turbo.config.PathConfiguration
import dev.hotwire.navigation.config.defaultFragmentDestination
import dev.hotwire.navigation.config.registerBridgeComponents
import dev.hotwire.navigation.destinations.HotwireDestination
import dev.hotwire.navigation.config.registerFragmentDestinations
import me.paolino.clusterheadachetracker.bridge.PrintComponent
import me.paolino.clusterheadachetracker.fragments.WebFragment
import me.paolino.clusterheadachetracker.fragments.WebModalFragment

class ClusterHeadacheTrackerApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        configureCookies()
        configureHotwire()
    }

    private fun configureCookies() {
        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            flush()
        }
    }

    private fun configureHotwire() {
        Hotwire.config.debugLoggingEnabled = BuildConfig.DEBUG
        Hotwire.config.webViewDebuggingEnabled = BuildConfig.DEBUG
        Hotwire.config.jsonConverter = KotlinXJsonConverter()
        Hotwire.config.applicationUserAgentPrefix = buildUserAgentPrefix()

        Hotwire.loadPathConfiguration(
            context = this,
            location = PathConfiguration.Location(
                assetFilePath = "json/path-configuration.json",
                remoteFileUrl = "${AppConfig.remoteBaseUrl}/configurations/android_v2.json",
            ),
        )

        Hotwire.defaultFragmentDestination = WebFragment::class
        Hotwire.registerFragmentDestinations(
            WebFragment::class,
            WebModalFragment::class,
        )
        Hotwire.registerBridgeComponents(*registeredBridgeComponents())
    }

    private fun buildUserAgentPrefix(): String {
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        val versionName = packageInfo.versionName
        val versionCode = PackageInfoCompat.getLongVersionCode(packageInfo)
        return "ClusterHeadacheTracker/$versionName.$versionCode;"
    }

    private fun registeredBridgeComponents(): Array<BridgeComponentFactory<HotwireDestination, BridgeComponent<HotwireDestination>>> {
        return Bridgework.coreComponents
            .plus(PrintComponent.factory)
    }
}
