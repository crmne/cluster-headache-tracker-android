package me.paolino.clusterheadachetracker

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.material.bottomnavigation.BottomNavigationView
import dev.hotwire.navigation.activities.HotwireActivity
import dev.hotwire.navigation.tabs.HotwireBottomNavigationController
import dev.hotwire.navigation.tabs.HotwireBottomTab
import dev.hotwire.navigation.tabs.navigatorConfigurations
import dev.hotwire.navigation.util.applyDefaultImeWindowInsets

class MainActivity : HotwireActivity(), AuthenticationCoordinator {
    private lateinit var bottomNavigationController: HotwireBottomNavigationController
    private lateinit var bottomNavigationView: BottomNavigationView

    private var lastSelectedRealTabIndex = MainTabs.defaultIndex
    private var suppressTabSelectionListener = false

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.root).applyDefaultImeWindowInsets()
        configureNavigatorInsets()
        configureBottomNavigation()
    }

    override fun navigatorConfigurations() = MainTabs.all.navigatorConfigurations

    override fun onAuthenticationRequired() {
        if (AppRoutes.isAuthenticationLocation(delegate.currentNavigator?.currentDestination?.location)) {
            return
        }

        resetShell {
            routeOnCurrentNavigator(AppRoutes.signInUrl)
        }
    }

    override fun onAuthenticationSucceeded() {
        resetShell()
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
        bottomNavigationController.load(MainTabs.all, MainTabs.defaultIndex)
        bottomNavigationController.setOnTabSelectedListener(::onTabSelected)
    }

    private fun onTabSelected(index: Int, @Suppress("UNUSED_PARAMETER") tab: HotwireBottomTab) {
        if (suppressTabSelectionListener) {
            return
        }

        if (MainTabs.isActionTab(index)) {
            restoreRealTabSelection()
            routeOnCurrentNavigator(AppRoutes.newHeadacheLogUrl)
            return
        }

        lastSelectedRealTabIndex = index
    }

    private fun resetShell(afterReset: (() -> Unit)? = null) {
        delegate.resetNavigators()
        restoreRealTabSelection()

        afterReset?.let { action ->
            window.decorView.post { action() }
        }
    }

    private fun restoreRealTabSelection() {
        suppressTabSelectionListener = true
        bottomNavigationController.selectTab(validRealTabIndex())
        suppressTabSelectionListener = false
    }

    private fun routeOnCurrentNavigator(location: String, attempt: Int = 0) {
        val navigator = delegate.currentNavigator
        if (navigator != null) {
            navigator.route(location)
            return
        }

        if (attempt < 5) {
            window.decorView.post {
                routeOnCurrentNavigator(location, attempt + 1)
            }
        }
    }

    private fun validRealTabIndex(): Int {
        return if (MainTabs.isActionTab(lastSelectedRealTabIndex)) {
            MainTabs.defaultIndex
        } else {
            lastSelectedRealTabIndex
        }
    }
}
