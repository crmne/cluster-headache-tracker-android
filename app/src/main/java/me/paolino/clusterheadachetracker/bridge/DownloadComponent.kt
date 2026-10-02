package me.paolino.clusterheadachetracker.bridge

import dev.hotwire.core.bridge.BridgeComponent
import dev.hotwire.core.bridge.BridgeComponentFactory
import dev.hotwire.core.bridge.BridgeDelegate
import dev.hotwire.core.bridge.Message
import dev.hotwire.navigation.destinations.HotwireDestination
import kotlinx.serialization.Serializable
import me.paolino.clusterheadachetracker.downloads.FileDownloads

/**
 * `download` bridge component: the web sends `{ url, title }` (as a `download` or `connect` event)
 * for a sign-in protected file (the PDF report);
 * we fetch it with the WebView's cookies and offer to open or share it.
 */
class DownloadComponent(name: String, private val bridgeDelegate: BridgeDelegate<HotwireDestination>) :
    BridgeComponent<HotwireDestination>(name, bridgeDelegate) {
    override fun onReceive(message: Message) {
        if (message.event !in EVENTS) return

        val data = message.data<MessageData>() ?: return
        val activity = bridgeDelegate.destination.fragment.activity ?: return
        FileDownloads.download(activity, data.url, title = data.title)
        replyTo(message.event)
    }

    @Serializable
    data class MessageData(val url: String, val title: String? = null)

    companion object {
        private val EVENTS = setOf("download", "connect")
        val factory = BridgeComponentFactory("download", ::DownloadComponent)
    }
}
