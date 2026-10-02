package me.paolino.clusterheadachetracker.fragments

import dev.hotwire.core.turbo.errors.VisitError
import dev.hotwire.core.turbo.webview.HotwireWebView
import dev.hotwire.navigation.destinations.HotwireDestinationDeepLink
import dev.hotwire.navigation.fragments.HotwireWebFragment

/**
 * Modal screens (sign-in, log forms, the current attack). A full-screen fragment in the modal context rather than
 * a bottom sheet: it gets a close button, and the bridge components from Joe Masilotti's library, which expect a
 * `HotwireFragment` toolbar (form, menu, button, share), work on these screens too.
 */
@HotwireDestinationDeepLink(uri = "hotwire://fragment/web/modal")
class WebModalFragment : HotwireWebFragment() {
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
