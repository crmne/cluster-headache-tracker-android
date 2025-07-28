package me.paolino.clusterheadachetracker.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dev.hotwire.core.turbo.errors.HttpError
import dev.hotwire.core.turbo.errors.VisitError
import dev.hotwire.core.turbo.visit.VisitOptions
import dev.hotwire.navigation.destinations.HotwireDestinationDeepLink
import dev.hotwire.navigation.fragments.HotwireWebFragment
import kotlinx.coroutines.launch
import me.paolino.clusterheadachetracker.BuildConfig
import me.paolino.clusterheadachetracker.util.AuthEvents
import me.paolino.clusterheadachetracker.util.SIGN_IN_URL

@HotwireDestinationDeepLink(uri = "hotwire://fragment/web")
class WebFragment : HotwireWebFragment() {

    companion object {
        const val TAG = "WebFragment"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG, "Creating view for location: $location")
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Delay WebView configuration to ensure it's fully initialized
        view.post {
            configureWebView()
        }
    }

    @Suppress("NestedBlockDepth")
    private fun configureWebView() {
        try {
            // Configure WebView settings for form submission
            navigator.session.webView.apply {
                settings.apply {
                    // Enable form data and autofill
                    saveFormData = true
                    domStorageEnabled = true

                    // Ensure JavaScript is enabled (should be by default with Hotwire)
                    javaScriptEnabled = true

                    // Allow mixed content for development
                    if (BuildConfig.DEBUG) {
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    }
                }
            }
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            Log.e(TAG, "Error configuring WebView", e)
        }
    }

    override fun onVisitCompleted(location: String, completedOffline: Boolean) {
        super.onVisitCompleted(location, completedOffline)

        // Check if we just completed a successful authentication
        // This can happen in several ways:
        // 1. Rails redirects to /recede_historical_location after sign in/sign up
        // 2. We navigate from sign_in to a main app page
        // 3. We navigate from sign_up to a main app page

        val isRecedeHistoricalLocation = location.contains("/recede_historical_location")
        val isFromSignIn = navigator.previousLocation?.contains("/sign_in") == true &&
            !location.contains("/sign_in") &&
            !location.contains("/sign_up")
        val isFromSignUp = navigator.previousLocation?.contains("/sign_up") == true &&
            !location.contains("/sign_in") &&
            !location.contains("/sign_up")

        if (isRecedeHistoricalLocation || isFromSignIn || isFromSignUp) {
            Log.d(TAG, "Authentication successful (location: $location), sending authentication changed event")
            val intent = Intent(AuthEvents.AUTHENTICATION_CHANGED)
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)

            // If this was a recede_historical_location, pop back to dismiss the modal
            if (isRecedeHistoricalLocation) {
                navigator.pop()
            }
        }
    }

    override fun onVisitErrorReceived(location: String, error: VisitError) {
        when (error) {
            is HttpError.ClientError.Unauthorized -> {
                Log.d(TAG, "Received 401 unauthorized error at: $location")
                handleUnauthorizedError()
            }
            else -> super.onVisitErrorReceived(location, error)
        }
    }

    // Bridge components are now managed differently in Hotwire Native 1.1
    // They register and unregister automatically through the BridgeDelegate

    // Menu handling is now done directly by bridge components
    // They manage their own menu items through the bridge delegate

    private fun handleUnauthorizedError() {
        Log.d(TAG, "Received 401 error, navigating to sign in")
        lifecycleScope.launch {
            // Navigate to sign in as a modal
            navigator.route(
                location = SIGN_IN_URL,
                options = VisitOptions(),
                bundle = Bundle().apply {
                    putString("presentation", "modal")
                },
            )
        }
    }
}
