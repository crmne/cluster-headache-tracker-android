package me.paolino.clusterheadachetracker

import android.os.Bundle
import dev.hotwire.core.config.Hotwire
import dev.hotwire.core.turbo.config.PathConfiguration
import dev.hotwire.navigation.activities.HotwireActivity
import dev.hotwire.navigation.navigator.NavigatorConfiguration

class MainActivity : HotwireActivity() {
    companion object {
        private const val BASE_URL = "https://clusterheadachetracker.com"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Hotwire.loadPathConfiguration(
            context = this,
            location = PathConfiguration.Location(
                assetFilePath = "json/android_v1.json",
//                remoteFileUrl = "$BASE_URL/configurations/android_v1.json"
            )
        )
    }

    override fun navigatorConfigurations() = listOf(
        NavigatorConfiguration(
            name = "main",
            startLocation = "$BASE_URL/headache_logs",
            navigatorHostId = R.id.main_nav_host
        )
    )
}