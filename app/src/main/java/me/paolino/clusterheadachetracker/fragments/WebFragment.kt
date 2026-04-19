package me.paolino.clusterheadachetracker.fragments

import dev.hotwire.core.turbo.errors.HttpError
import dev.hotwire.core.turbo.errors.VisitError
import dev.hotwire.navigation.destinations.HotwireDestinationDeepLink
import dev.hotwire.navigation.fragments.HotwireWebFragment
import me.paolino.clusterheadachetracker.MainActivity

@HotwireDestinationDeepLink(uri = "hotwire://fragment/web")
class WebFragment : HotwireWebFragment() {
    override fun onVisitCompleted(location: String, completedOffline: Boolean) {
        super.onVisitCompleted(location, completedOffline)
        (activity as? MainActivity)?.checkAuthenticationCompleted(location)
    }

    override fun onVisitErrorReceived(location: String, error: VisitError) {
        when (error) {
            is HttpError.ClientError.Unauthorized ->
                (activity as? MainActivity)?.presentAuthentication()
            else -> super.onVisitErrorReceived(location, error)
        }
    }
}
