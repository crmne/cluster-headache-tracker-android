package me.paolino.clusterheadachetracker

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.VisibleForTesting
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.material.bottomnavigation.BottomNavigationView
import dev.hotwire.core.turbo.webview.WebViewInfo
import dev.hotwire.core.turbo.webview.WebViewVersionCompatibility
import dev.hotwire.navigation.activities.HotwireActivity
import dev.hotwire.navigation.navigator.Navigator
import dev.hotwire.navigation.tabs.HotwireBottomNavigationController
import dev.hotwire.navigation.tabs.HotwireBottomTab
import dev.hotwire.navigation.tabs.navigatorConfigurations

class MainActivity : HotwireActivity() {
    private lateinit var bottomNavigationController: HotwireBottomNavigationController
    private lateinit var bottomNavigationView: BottomNavigationView
    private val tabs by lazy { MainTabs.all(this) }

    private var lastSelectedRealTabIndex = MainTabs.DEFAULT_INDEX
    private var suppressTabSelectionListener = false
    private var isAuthenticating = false
    private var pendingDeepLinkUrl: String? = null

    /** The last shortcut/widget deep link handed to a navigator. */
    @VisibleForTesting
    var routedDeepLinkUrl: String? = null
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lastSelectedRealTabIndex = savedInstanceState?.getInt(SELECTED_TAB_KEY) ?: MainTabs.DEFAULT_INDEX
        configureBottomNavigation()

        if (savedInstanceState == null) {
            pendingDeepLinkUrl = DeepLinks.urlFrom(intent)
        }

        WebViewVersionCompatibility.displayUpdateDialogIfOutdated(
            activity = this,
            requiredVersion = WebViewInfo.REQUIRED_WEBVIEW_VERSION,
        )
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        DeepLinks.urlFrom(intent)?.let { url ->
            pendingDeepLinkUrl = url
            routePendingDeepLink()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SELECTED_TAB_KEY, lastSelectedRealTabIndex)
    }

    override fun navigatorConfigurations() = tabs.navigatorConfigurations

    override fun onNavigatorReady(navigator: Navigator) {
        super.onNavigatorReady(navigator)
        if (navigator != delegate.currentNavigator || pendingDeepLinkUrl == null) return

        // The root destination exists but its view (and web delegate) doesn't yet: route once it does.
        val root = navigator.currentDestination?.fragment ?: return
        root.viewLifecycleOwnerLiveData.observe(this) { owner ->
            if (owner != null) window.decorView.post { routePendingDeepLink() }
        }
    }

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

    private fun routePendingDeepLink() {
        val url = pendingDeepLinkUrl ?: return
        val navigator =
            delegate.currentNavigator?.takeIf { it.isReady() && it.currentDestination?.fragment?.view != null }
                ?: return

        pendingDeepLinkUrl = null
        routedDeepLinkUrl = url
        navigator.route(url)
    }

    private fun configureBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_nav)

        bottomNavigationController = HotwireBottomNavigationController(
            activity = this,
            view = bottomNavigationView,
            lazyLoadTabs = true,
        )
        bottomNavigationController.load(tabs, lastSelectedRealTabIndex)
        bottomNavigationController.setOnTabSelectedListener(::onTabSelected)
    }

    private fun onTabSelected(index: Int, @Suppress("UNUSED_PARAMETER") tab: HotwireBottomTab) {
        if (suppressTabSelectionListener) return

        if (MainTabs.isActionTab(index)) {
            // The bottom navigation marks the tapped item as selected after this listener returns, so restore the
            // real tab (and open the form on it) on the next frame.
            bottomNavigationView.post {
                restoreRealTabSelection()
                delegate.currentNavigator?.route(AppRoutes.newHeadacheLogUrl)
            }
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

    private companion object {
        const val SELECTED_TAB_KEY = "selected_tab_index"
    }
}
