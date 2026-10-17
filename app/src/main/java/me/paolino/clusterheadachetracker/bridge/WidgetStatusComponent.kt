package me.paolino.clusterheadachetracker.bridge

import dev.hotwire.core.bridge.BridgeComponent
import dev.hotwire.core.bridge.BridgeComponentFactory
import dev.hotwire.core.bridge.BridgeDelegate
import dev.hotwire.core.bridge.Message
import dev.hotwire.navigation.destinations.HotwireDestination
import me.paolino.clusterheadachetracker.widget.WidgetStatus
import me.paolino.clusterheadachetracker.widget.WidgetUpdates

/**
 * Receives the `widget-status` payload signed-in pages send (`connect`) and hands it to the home-screen widget and
 * shortcuts. Signed-out pages send `clear` (after a sign-out elsewhere, an expired session or a deleted account), which
 * forgets the last account's status.
 */
class WidgetStatusComponent(name: String, private val bridgeDelegate: BridgeDelegate<HotwireDestination>) :
    BridgeComponent<HotwireDestination>(name, bridgeDelegate) {
    override fun onReceive(message: Message) {
        val context = bridgeDelegate.destination.fragment.context ?: return

        when (message.event) {
            "connect" -> message.data<WidgetStatus>()?.let { WidgetUpdates.publish(context, it) }
            "clear" -> WidgetUpdates.clear(context)
        }
    }

    companion object {
        val factory = BridgeComponentFactory("widget-status", ::WidgetStatusComponent)
    }
}
