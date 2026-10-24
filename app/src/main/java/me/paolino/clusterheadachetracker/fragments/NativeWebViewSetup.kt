package me.paolino.clusterheadachetracker.fragments

import androidx.fragment.app.Fragment
import dev.hotwire.core.turbo.errors.HttpError
import dev.hotwire.core.turbo.errors.VisitError
import dev.hotwire.core.turbo.webview.HotwireWebView
import dev.hotwire.navigation.fragments.HotwireWebFragment
import me.paolino.clusterheadachetracker.MainActivity
import me.paolino.clusterheadachetracker.downloads.FileDownloads

/** Behavior shared by full-screen and modal web fragments. */
internal fun Fragment.attachDownloadListener(webView: HotwireWebView) {
    webView.setDownloadListener { url, _, contentDisposition, mimeType, _ ->
        activity?.let { FileDownloads.download(it, url, contentDisposition, mimeType) }
    }
}

internal fun HotwireWebFragment.onVisitCompletedForAuthentication(location: String) {
    (activity as? MainActivity)?.checkAuthenticationCompleted(navigator, location)
}

/** Returns true when the error was handled by presenting sign-in. */
internal fun Fragment.handleAuthenticationError(error: VisitError): Boolean {
    if (error !is HttpError.ClientError.Unauthorized) return false
    (activity as? MainActivity)?.presentAuthentication()
    return true
}
