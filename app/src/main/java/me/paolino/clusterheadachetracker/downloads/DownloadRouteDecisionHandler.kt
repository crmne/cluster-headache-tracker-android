package me.paolino.clusterheadachetracker.downloads

import dev.hotwire.core.turbo.visit.VisitProposal
import dev.hotwire.navigation.activities.HotwireActivity
import dev.hotwire.navigation.navigator.NavigatorConfiguration
import dev.hotwire.navigation.routing.Router

/** Downloads same-host PDF and CSV links instead of trying to render them in the WebView. */
class DownloadRouteDecisionHandler : Router.RouteDecisionHandler {
    override val name = "download"

    override fun matches(proposal: VisitProposal, configuration: NavigatorConfiguration): Boolean =
        FileDownloads.isAppLocation(proposal.location) && FileDownloads.isDownloadLocation(proposal.location)

    override fun handle(
        proposal: VisitProposal,
        configuration: NavigatorConfiguration,
        activity: HotwireActivity,
    ): Router.Decision {
        FileDownloads.download(activity, proposal.location)
        return Router.Decision.CANCEL
    }
}
