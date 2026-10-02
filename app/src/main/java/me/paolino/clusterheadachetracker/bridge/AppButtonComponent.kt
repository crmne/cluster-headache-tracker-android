package me.paolino.clusterheadachetracker.bridge

import android.content.Intent
import android.print.PrintManager
import android.view.Menu
import android.view.MenuItem
import android.webkit.CookieManager
import androidx.core.net.toUri
import dev.hotwire.core.bridge.BridgeComponent
import dev.hotwire.core.bridge.BridgeComponentFactory
import dev.hotwire.core.bridge.BridgeDelegate
import dev.hotwire.core.bridge.Message
import dev.hotwire.navigation.destinations.HotwireDestination
import dev.hotwire.navigation.fragments.HotwireFragment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.paolino.clusterheadachetracker.MainActivity
import me.paolino.clusterheadachetracker.R
import me.paolino.clusterheadachetracker.widget.WidgetUpdates

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
            "connect", "left", "right" -> addButton(message)
            "disconnect" -> removeButton()
        }
    }

    private fun addButton(message: Message) {
        val data = message.data<MessageData>() ?: return
        val toolbar = fragment.toolbarForNavigation() ?: return

        removeButton()
        toolbar.menu.add(0, MENU_ITEM_ID, Menu.NONE, data.title).apply {
            setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            // Material icon when we know the requested `androidImage`; the title stays as its accessibility label.
            iconFor(data.imageName)?.let { setIcon(it) }
            contentDescription = data.title
            setOnMenuItemClickListener {
                handleTap(data, message.event)
                true
            }
        }
    }

    private fun iconFor(imageName: String?): Int? = when (imageName) {
        "add" -> R.drawable.ic_toolbar_add
        "print" -> R.drawable.ic_toolbar_print
        "logout" -> R.drawable.ic_toolbar_logout
        else -> null
    }

    private fun handleTap(data: MessageData, event: String) {
        when (data.action) {
            Action.PRINT -> {
                replyTo(event)
                printCurrentPage()
            }

            Action.SIGN_OUT -> {
                replyTo(event)
                signOut()
            }

            Action.SPONSOR -> openExternally("https://github.com/sponsors/crmne")

            Action.DEFAULT -> replyTo(event)
        }
    }

    private fun printCurrentPage() {
        val webView = bridgeDelegate.destination.navigator.session.webView
        val printAdapter = webView.createPrintDocumentAdapter("Cluster Headache Tracker")
        val printManager = fragment.requireContext().getSystemService(PrintManager::class.java)
        printManager?.print("Cluster Headache Tracker", printAdapter, null)
    }

    private fun signOut() {
        WidgetUpdates.clear(fragment.requireContext())
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()

        val activity = fragment.activity as? MainActivity ?: return
        activity.window.decorView.postDelayed({
            activity.signOut()
        }, SIGN_OUT_DELAY_MS)
    }

    private fun openExternally(url: String) {
        fragment.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    }

    private fun removeButton() {
        fragment.toolbarForNavigation()?.menu?.removeItem(MENU_ITEM_ID)
    }

    enum class Action { PRINT, SIGN_OUT, SPONSOR, DEFAULT }

    @Serializable
    data class MessageData(
        val title: String,
        @SerialName("androidImage") val imageName: String? = null,
        @SerialName("color") val colorCode: String? = null,
        val nativeAction: String? = null,
    ) {
        /**
         * Titles are translated, so the language-independent `nativeAction` decides; older servers don't send it,
         * so the Material Symbols name and then the English title are fallbacks.
         */
        val action: Action
            get() = when (nativeAction) {
                "print" -> Action.PRINT
                "sign-out" -> Action.SIGN_OUT
                "sponsor" -> Action.SPONSOR
                else -> legacyAction
            }

        private val legacyAction: Action
            get() = when {
                imageName == "print" || title == "Print" -> Action.PRINT
                imageName == "logout" || title == "Sign Out" -> Action.SIGN_OUT
                title == "Sponsor" -> Action.SPONSOR
                else -> Action.DEFAULT
            }
    }
}
