package me.paolino.clusterheadachetracker.bridge

import dev.hotwire.core.bridge.BridgeComponent
import dev.hotwire.core.bridge.BridgeComponentFactory
import dev.hotwire.core.bridge.BridgeDelegate
import dev.hotwire.core.bridge.Message
import dev.hotwire.navigation.destinations.HotwireDestination
import me.paolino.clusterheadachetracker.widget.WidgetStatus
import me.paolino.clusterheadachetracker.widget.WidgetUpdates

/**
 * Receives the `widget-status` payload the web app sends from the logs dashboard and `/current_attack`,
 * and hands it to the home-screen widget and shortcuts.
 */
class WidgetStatusComponent(name: String, private val bridgeDelegate: BridgeDelegate<HotwireDestination>) :
    BridgeComponent<HotwireDestination>(name, bridgeDelegate) {
    override fun onReceive(message: Message) {
        if (message.event != "connect") return

        val status = message.data<WidgetStatus>() ?: return
        val context = bridgeDelegate.destination.fragment.context ?: return
        WidgetUpdates.publish(context, status)
    }

    companion object {
        val factory = BridgeComponentFactory("widget-status", ::WidgetStatusComponent)
    }
}
