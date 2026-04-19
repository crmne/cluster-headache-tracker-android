package me.paolino.clusterheadachetracker.bridge

import android.content.Intent
import android.net.Uri
import android.print.PrintManager
import android.view.Menu
import android.view.MenuItem
import android.webkit.CookieManager
import dev.hotwire.core.bridge.BridgeComponent
import dev.hotwire.core.bridge.BridgeComponentFactory
import dev.hotwire.core.bridge.BridgeDelegate
import dev.hotwire.core.bridge.Message
import dev.hotwire.navigation.destinations.HotwireDestination
import dev.hotwire.navigation.fragments.HotwireFragment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.paolino.clusterheadachetracker.MainActivity

class AppButtonComponent(name: String, private val bridgeDelegate: BridgeDelegate<HotwireDestination>) :
    BridgeComponent<HotwireDestination>(name, bridgeDelegate) {
    companion object {
        val factory = BridgeComponentFactory("button", ::AppButtonComponent)
        private const val MENU_ITEM_ID = 9001
        private const val SIGN_OUT_DELAY_MS = 350L
    }

    private val fragment: HotwireFragment
        get() = bridgeDelegate.destination.fragment as HotwireFragment

    override fun onReceive(message: Message) {
        when (message.event) {
            "connect", "right" -> addButton(message)
            "disconnect" -> removeButton()
        }
    }

    private fun addButton(message: Message) {
        val data = message.data<MessageData>() ?: return
        val toolbar = fragment.toolbarForNavigation() ?: return

        removeButton()
        toolbar.menu.add(0, MENU_ITEM_ID, Menu.NONE, data.title).apply {
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            setOnMenuItemClickListener {
                handleTap(data, message.event)
                true
            }
        }
    }

    private fun handleTap(data: MessageData, event: String) {
        when (data.title) {
            "Print" -> {
                replyTo(event)
                printCurrentPage()
            }
            "Sign Out" -> {
                replyTo(event)
                signOut()
            }
            "Sponsor" -> {
                openExternally("https://github.com/sponsors/crmne")
            }
            else -> replyTo(event)
        }
    }

    private fun printCurrentPage() {
        val webView = bridgeDelegate.destination.navigator.session.webView
        val printAdapter = webView.createPrintDocumentAdapter("Cluster Headache Tracker")
        val printManager = fragment.requireContext().getSystemService(PrintManager::class.java)
        printManager?.print("Cluster Headache Tracker", printAdapter, null)
    }

    private fun signOut() {
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()

        val activity = fragment.activity as? MainActivity ?: return
        activity.window.decorView.postDelayed({
            activity.signOut()
        }, SIGN_OUT_DELAY_MS)
    }

    private fun openExternally(url: String) {
        fragment.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    private fun removeButton() {
        fragment.toolbarForNavigation()?.menu?.removeItem(MENU_ITEM_ID)
    }

    @Serializable
    data class MessageData(
        val title: String,
        @SerialName("androidImage") val imageName: String?,
        @SerialName("color") val colorCode: String?,
    )
}
