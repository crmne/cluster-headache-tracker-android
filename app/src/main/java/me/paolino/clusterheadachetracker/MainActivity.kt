package me.paolino.clusterheadachetracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import dev.hotwire.core.turbo.visit.VisitOptions
import dev.hotwire.navigation.activities.HotwireActivity
import dev.hotwire.navigation.tabs.HotwireBottomNavigationController
import dev.hotwire.navigation.tabs.navigatorConfigurations
import me.paolino.clusterheadachetracker.util.AuthEvents
import me.paolino.clusterheadachetracker.util.NEW_HEADACHE_LOG_URL
import me.paolino.clusterheadachetracker.util.SIGN_IN_URL

@Suppress("TooManyFunctions")
class MainActivity : HotwireActivity() {
    private lateinit var bottomNavigationController: HotwireBottomNavigationController

    companion object {
        const val TAG = "MainActivity"
        const val NEW_TAB_INDEX = 2 // Index of the "New" tab
        private const val SIGN_IN_MODAL_DELAY_MS = 500L
        private const val SIGN_IN_NAVIGATION_DELAY_MS = 100L
        private const val TAB_REFRESH_DELAY_MS = 100L
    }

    private var authenticationChanged = false
    private val refreshedTabs = mutableSetOf<Int>()

    private val activity: HotwireActivity
        get() = this@MainActivity

    private val authReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                AuthEvents.SIGN_OUT_REQUESTED -> handleSignOut()
                AuthEvents.AUTHENTICATION_CHANGED -> refreshAllTabs()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        configureAppearance()
        initializeBottomTabs()
        configureEdgeToEdge()
        registerReceivers()

        // Check if we need to show sign in modal after recreation
        if (intent.getBooleanExtra("show_sign_in", false)) {
            Handler(Looper.getMainLooper()).postDelayed({
                activity.delegate.currentNavigator?.route(
                    location = SIGN_IN_URL,
                    options = VisitOptions(),
                    bundle = Bundle().apply {
                        putString("presentation", "modal")
                    },
                )
            }, SIGN_IN_MODAL_DELAY_MS)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(authReceiver)
    }

    private fun configureAppearance() {
        // Primary color from Rails app (#4f46e5)
        val primaryColor = Color.parseColor("#4f46e5")

        // Configure status bar color
        window.statusBarColor = primaryColor
    }

    private fun initializeBottomTabs() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav)

        // Style the bottom navigation
        bottomNavigationView.itemIconTintList = getColorStateList(R.color.bottom_nav_color)
        bottomNavigationView.itemTextColor = getColorStateList(R.color.bottom_nav_color)

        bottomNavigationController = HotwireBottomNavigationController(this, bottomNavigationView)
        bottomNavigationController.load(mainTabs, 0)

        // Handle special "New" tab behavior like iOS
        bottomNavigationController.setOnTabSelectedListener { index, tab ->
            if (index == NEW_TAB_INDEX) {
                // Don't actually switch to the "New" tab
                // Instead, navigate to new headache log on the current tab
                val currentNavigator = activity.delegate.currentNavigator
                currentNavigator?.route(
                    location = NEW_HEADACHE_LOG_URL,
                    options = VisitOptions(),
                    bundle = Bundle().apply {
                        putString("presentation", "modal")
                    },
                )

                // Return to the previous tab - stay on current tab
                false // Don't select the "New" tab
            } else {
                // Check if this tab needs refresh after authentication
                if (authenticationChanged && !refreshedTabs.contains(index)) {
                    Log.d(TAG, "Refreshing tab $index after authentication change")
                    refreshedTabs.add(index)

                    Handler(Looper.getMainLooper()).postDelayed({
                        val navigatorHost = supportFragmentManager.findFragmentById(
                            tab.configuration.navigatorHostId,
                        ) as? dev.hotwire.navigation.navigator.NavigatorHost

                        // Navigate to the tab's default URL
                        navigatorHost?.navigator?.route(
                            location = tab.configuration.startLocation,
                            options = VisitOptions(),
                        )

                        // If all tabs have been refreshed, reset the flag
                        if (refreshedTabs.size >= mainTabs.size - 1) { // -1 for the "New" tab
                            authenticationChanged = false
                            refreshedTabs.clear()
                        }
                    }, TAB_REFRESH_DELAY_MS)
                }
                true // Allow normal tab selection
            }
        }
    }

    override fun navigatorConfigurations() = mainTabs.navigatorConfigurations

    private fun configureEdgeToEdge() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav)

        // Apply window insets to bottom navigation
        ViewCompat.setOnApplyWindowInsetsListener(bottomNavigationView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                view.paddingLeft,
                view.paddingTop,
                view.paddingRight,
                systemBars.bottom,
            )
            insets
        }
    }

    private fun registerReceivers() {
        val intentFilter = IntentFilter().apply {
            addAction(AuthEvents.SIGN_OUT_REQUESTED)
            addAction(AuthEvents.AUTHENTICATION_CHANGED)
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(authReceiver, intentFilter)
    }

    private fun handleSignOut() {
        Log.d(TAG, "Handling sign out")

        // Clear cookies and WebView data
        clearWebViewData()

        // Mark that authentication has changed
        authenticationChanged = true
        refreshedTabs.clear()

        // Reset all navigators to their start locations
        mainTabs.forEach { tab ->
            val navigatorHost = supportFragmentManager.findFragmentById(
                tab.configuration.navigatorHostId,
            ) as? dev.hotwire.navigation.navigator.NavigatorHost

            navigatorHost?.navigator?.reset()
        }

        // Navigate to sign in on the current navigator
        Handler(Looper.getMainLooper()).postDelayed({
            activity.delegate.currentNavigator?.route(
                location = SIGN_IN_URL,
                options = VisitOptions(),
                bundle = Bundle().apply {
                    putString("presentation", "modal")
                },
            )
        }, SIGN_IN_NAVIGATION_DELAY_MS)
    }

    private fun refreshAllTabs() {
        Log.d(TAG, "Authentication state changed - marking tabs for refresh")

        // Mark that authentication has changed
        authenticationChanged = true
        refreshedTabs.clear()

        // Get current tab index
        val currentTabIndex = bottomNavigationController.view.selectedItemId

        // Refresh the current tab immediately
        if (currentTabIndex != NEW_TAB_INDEX) {
            refreshedTabs.add(currentTabIndex)
            val currentTab = mainTabs[currentTabIndex]
            activity.delegate.currentNavigator?.route(
                location = currentTab.configuration.startLocation,
                options = VisitOptions(),
            )
        }
    }

    private fun clearWebViewData() {
        Log.d(TAG, "Clearing WebView data")

        // Clear cookies
        android.webkit.CookieManager.getInstance().apply {
            removeAllCookies(null)
            flush()
        }

        // Clear WebStorage
        android.webkit.WebStorage.getInstance().deleteAllData()
    }
}
