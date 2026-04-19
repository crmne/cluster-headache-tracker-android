package me.paolino.clusterheadachetracker.fragments

import dev.hotwire.core.turbo.errors.HttpError
import dev.hotwire.core.turbo.errors.VisitError
import dev.hotwire.navigation.destinations.HotwireDestinationDeepLink
import dev.hotwire.navigation.fragments.HotwireWebBottomSheetFragment
import me.paolino.clusterheadachetracker.AppRoutes
import me.paolino.clusterheadachetracker.AuthenticationCoordinator

@HotwireDestinationDeepLink(uri = "hotwire://fragment/web/modal")
class WebModalFragment : HotwireWebBottomSheetFragment() {
    private val authenticationCoordinator: AuthenticationCoordinator?
        get() = activity as? AuthenticationCoordinator

    override fun onVisitCompleted(location: String, completedOffline: Boolean) {
        super.onVisitCompleted(location, completedOffline)

        if (AppRoutes.didCompleteAuthentication(location, navigator.previousLocation)) {
            navigator.pop()
            authenticationCoordinator?.onAuthenticationSucceeded()
        }
    }

    override fun onVisitErrorReceived(location: String, error: VisitError) {
        when (error) {
            is HttpError.ClientError.Unauthorized -> authenticationCoordinator?.onAuthenticationRequired()
            else -> super.onVisitErrorReceived(location, error)
        }
    }
}
