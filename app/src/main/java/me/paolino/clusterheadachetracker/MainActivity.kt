package me.paolino.clusterheadachetracker

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.annotation.VisibleForTesting
import androidx.appcompat.widget.TooltipCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dev.hotwire.core.turbo.webview.WebViewInfo
import dev.hotwire.core.turbo.webview.WebViewVersionCompatibility
import dev.hotwire.navigation.activities.HotwireActivity
import dev.hotwire.navigation.navigator.Navigator
import me.paolino.clusterheadachetracker.shell.ShellTab
import me.paolino.clusterheadachetracker.shell.TabShell

/**
 * The app's one activity: a navigator per tab in the floating toolbar, New
 * log beside it, and signing in.
 *
 * Signing in happens in a modal over the current tab. Any tab's 401 asks for
 * it ([presentAuthentication]); the form opens once, when a tab can show it,
 * and every tab starts afresh once the modal leaves the sign-in pages
 * ([checkAuthenticationCompleted]). Signing out starts every tab afresh too,
 * in this activity, so no web view is left behind.
 */
class MainActivity : HotwireActivity() {
    private lateinit var tabs: TabShell
    private lateinit var root: View
    private lateinit var hosts: View
    private lateinit var dock: View

    private var isAuthenticating = false

    /** A 401 arrived before any tab could show the sign-in form: show it once one can. */
    private var authenticationWaiting = false

    /** The form was asked for and is on its way (routing is asynchronous): don't ask twice. */
    private var signInRouted = false
    private var pendingDeepLinkUrl: String? = null

    private var keyboardVisible = false
    private var bottomInset = 0

    /** The last shortcut/widget deep link handed to a navigator. */
    @VisibleForTesting
    var routedDeepLinkUrl: String? = null
        private set

    @VisibleForTesting
    val selectedTab: ShellTab
        get() = tabs.selected

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        root = findViewById(R.id.root)
        hosts = findViewById(R.id.navigator_hosts)
        dock = findViewById(R.id.dock)

        // A sign-in form Android restored with the activity is still the one
        // asked for: don't stack another on it.
        isAuthenticating = savedInstanceState?.getBoolean(AUTHENTICATING_KEY) ?: false
        setUpTabs(MainTabs.named(savedInstanceState?.getString(SELECTED_TAB_KEY)) ?: MainTabs.logs)
        setUpNewLogButton()
        setUpInsets()

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
        outState.putString(SELECTED_TAB_KEY, MainTabs.nameOf(tabs.selected))
        outState.putBoolean(AUTHENTICATING_KEY, isAuthenticating)
    }

    /**
     * Hotwire Native gives each navigator a web view of its own and never
     * destroys it, so each time this activity goes (recreated for dark mode,
     * finished) its web views would stay alive. They go with it.
     */
    override fun onDestroy() {
        val webViews = MainTabs.shellTabs.mapNotNull {
            delegate.findNavigatorHost(it.hostId)?.navigator?.session?.webView
        }
        super.onDestroy()
        webViews.forEach { webView ->
            (webView.parent as? ViewGroup)?.removeView(webView)
            webView.destroy()
        }
    }

    override fun navigatorConfigurations() = MainTabs.navigatorConfigurations

    override fun onNavigatorReady(navigator: Navigator) {
        super.onNavigatorReady(navigator)
        navigator.host.navController.addOnDestinationChangedListener { _, _, _ ->
            signInRouted = false
            // The destination is attached once the transaction runs.
            root.post(::updateChrome)
        }
        val waiting = pendingDeepLinkUrl != null || authenticationWaiting
        // The root destination exists but its view (and web delegate) doesn't yet: go on once it does.
        val rootDestination = navigator.currentDestination?.fragment
        if (navigator != delegate.currentNavigator || !waiting || rootDestination == null) return
        rootDestination.viewLifecycleOwnerLiveData.observe(this) { owner ->
            if (owner != null) {
                window.decorView.post {
                    if (authenticationWaiting) presentAuthentication()
                    routePendingDeepLink()
                }
            }
        }
    }

    /**
     * A page answered 401: open the sign-in form over the current tab, unless
     * it's already showing or on its way, so several tabs' 401s (or a form
     * Android restored with the activity) never stack forms. Nothing counts
     * as started until a tab can show the form; until then the request
     * waits, so a 401 that arrives while the tabs are still loading can't
     * leave the app on a blank page with no form. A form closed without
     * signing in comes back with the next 401: every page needs an account.
     */
    fun presentAuthentication() {
        val navigator = readyNavigator()
        if (navigator == null) {
            authenticationWaiting = true
            return
        }

        authenticationWaiting = false
        isAuthenticating = true
        if (signInRouted || AppRoutes.isAuthenticationLocation(navigator.location)) return
        signInRouted = true
        navigator.route(AppRoutes.signInUrl)
    }

    /**
     * Signing out: every tab starts afresh on Logs and finds no session, so
     * the sign-in form opens. Recreating the activity instead would leave
     * every tab's web view behind and load all of them at once.
     */
    fun signOut() {
        isAuthenticating = false
        authenticationWaiting = false
        signInRouted = false
        tabs.select(MainTabs.logs)
        delegate.resetNavigators()
        updateChrome()
    }

    fun checkAuthenticationCompleted(location: String) {
        if (!isAuthenticating) return
        if (AppRoutes.isAuthenticationLocation(location)) return

        isAuthenticating = false
        delegate.resetNavigators()
    }

    private fun setUpTabs(selected: ShellTab) {
        tabs =
            TabShell(
                activity = this,
                tabs = MainTabs.shellTabs,
                buttons = findViewById(R.id.tab_buttons),
                dock = dock,
                buttonLayout = R.layout.view_floating_tab,
            )
        tabs.load(selected)
        tabs.onTabSelected = { updateChrome() }
        tabs.onTabReselected = { tab -> delegate.findNavigatorHost(tab.hostId)?.navigator?.clearAll() }
    }

    /** New log opens the form over the tab that's showing, as the "New" tab did. */
    private fun setUpNewLogButton() {
        val button = findViewById<FloatingActionButton>(R.id.new_log)
        TooltipCompat.setTooltipText(button, getString(R.string.new_log))
        button.setOnClickListener { delegate.currentNavigator?.route(AppRoutes.newHeadacheLogUrl) }
    }

    /**
     * The toolbar and New log show on each tab's first page and hide on pages
     * pushed from it, in modals and under the keyboard. On a tab's first
     * page the page scrolls under them; the server leaves room under its
     * last row.
     */
    private fun updateChrome() {
        val navigator = delegate.currentNavigator
        val modal = navigator?.currentDestination?.isModal == true
        val atStart = navigator?.isAtStartDestination() != false
        tabs.setShown(atStart && !modal && !keyboardVisible)

        hosts.updatePadding(bottom = if (keyboardVisible) 0 else bottomInset)
        dock.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            bottomMargin = bottomInset + resources.getDimensionPixelSize(R.dimen.dock_margin)
        }
    }

    private fun setUpInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            keyboardVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            bottomInset = bars.bottom
            hosts.updatePadding(left = bars.left, right = bars.right)
            // The keyboard pushes the pages up, so forms stay above it.
            view.updatePadding(bottom = if (keyboardVisible) ime.bottom else 0)
            updateChrome()
            insets
        }
    }

    /** The current tab's navigator, once its first page can take a visit. */
    private fun readyNavigator(): Navigator? =
        delegate.currentNavigator?.takeIf { it.isReady() && it.currentDestination?.fragment?.view != null }

    private fun routePendingDeepLink() {
        val url = pendingDeepLinkUrl ?: return
        val navigator = readyNavigator() ?: return

        pendingDeepLinkUrl = null
        routedDeepLinkUrl = url
        navigator.route(url)
    }

    private companion object {
        const val SELECTED_TAB_KEY = "selected_tab"
        const val AUTHENTICATING_KEY = "authenticating"
    }
}
