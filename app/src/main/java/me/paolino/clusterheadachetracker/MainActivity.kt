package me.paolino.clusterheadachetracker

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.material.bottomnavigation.BottomNavigationView
import dev.hotwire.navigation.activities.HotwireActivity
import dev.hotwire.navigation.tabs.HotwireBottomNavigationController
import dev.hotwire.navigation.tabs.HotwireBottomTab
import dev.hotwire.navigation.tabs.navigatorConfigurations
import dev.hotwire.navigation.util.applyDefaultImeWindowInsets

class MainActivity : HotwireActivity() {
    private lateinit var bottomNavigationController: HotwireBottomNavigationController
    private lateinit var bottomNavigationView: BottomNavigationView

    private var lastSelectedRealTabIndex = MainTabs.DEFAULT_INDEX
    private var suppressTabSelectionListener = false
    private var isAuthenticating = false

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.root).applyDefaultImeWindowInsets()
        configureNavigatorInsets()
        configureBottomNavigation()
    }

    override fun navigatorConfigurations() = MainTabs.all.navigatorConfigurations

    fun presentAuthentication() {
        if (isAuthenticating) return
        isAuthenticating = true
        delegate.currentNavigator?.route(AppRoutes.signInUrl)
    }

    fun signOut() {
        recreate()
    }

    fun checkAuthenticationCompleted(location: String) {
        if (!isAuthenticating) return
        if (AppRoutes.isAuthenticationLocation(location)) return

        isAuthenticating = false
        delegate.resetNavigators()
        restoreRealTabSelection()
    }

    private fun configureNavigatorInsets() {
        MainTabs.all.forEach { tab ->
            findViewById<View>(tab.configuration.navigatorHostId).applyDefaultImeWindowInsets()
        }
    }

    private fun configureBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_nav)
        bottomNavigationView.itemIconTintList =
            AppCompatResources.getColorStateList(this, R.color.bottom_nav_color)
        bottomNavigationView.itemTextColor =
            AppCompatResources.getColorStateList(this, R.color.bottom_nav_color)

        bottomNavigationController = HotwireBottomNavigationController(this, bottomNavigationView)
        bottomNavigationController.load(MainTabs.all, MainTabs.DEFAULT_INDEX)
        bottomNavigationController.setOnTabSelectedListener(::onTabSelected)
    }

    private fun onTabSelected(index: Int, @Suppress("UNUSED_PARAMETER") tab: HotwireBottomTab) {
        if (suppressTabSelectionListener) return

        if (MainTabs.isActionTab(index)) {
            restoreRealTabSelection()
            delegate.currentNavigator?.route(AppRoutes.newHeadacheLogUrl)
            return
        }

        lastSelectedRealTabIndex = index
    }

    private fun restoreRealTabSelection() {
        suppressTabSelectionListener = true
        bottomNavigationController.selectTab(
            if (MainTabs.isActionTab(lastSelectedRealTabIndex)) {
                MainTabs.DEFAULT_INDEX
            } else {
                lastSelectedRealTabIndex
            },
        )
        suppressTabSelectionListener = false
    }
}
