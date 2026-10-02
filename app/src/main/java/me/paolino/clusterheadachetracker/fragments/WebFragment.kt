package me.paolino.clusterheadachetracker.fragments

import dev.hotwire.core.turbo.errors.VisitError
import dev.hotwire.core.turbo.webview.HotwireWebView
import dev.hotwire.navigation.destinations.HotwireDestinationDeepLink
import dev.hotwire.navigation.fragments.HotwireWebFragment

@HotwireDestinationDeepLink(uri = "hotwire://fragment/web")
class WebFragment : HotwireWebFragment() {
    override fun onWebViewAttached(webView: HotwireWebView) {
        super.onWebViewAttached(webView)
        attachDownloadListener(webView)
    }

    override fun onVisitCompleted(location: String, completedOffline: Boolean) {
        super.onVisitCompleted(location, completedOffline)
        onVisitCompletedForAuthentication(location)
    }

    override fun onVisitErrorReceived(location: String, error: VisitError) {
        if (!handleAuthenticationError(error)) super.onVisitErrorReceived(location, error)
    }
}
