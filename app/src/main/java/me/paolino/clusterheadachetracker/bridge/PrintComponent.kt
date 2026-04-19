package me.paolino.clusterheadachetracker.bridge

import android.print.PrintManager
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.appbar.MaterialToolbar
import dev.hotwire.core.bridge.BridgeComponent
import dev.hotwire.core.bridge.BridgeComponentFactory
import dev.hotwire.core.bridge.BridgeDelegate
import dev.hotwire.core.bridge.Message
import dev.hotwire.navigation.destinations.HotwireDestination
import dev.hotwire.navigation.fragments.HotwireFragment

class PrintComponent(
    name: String,
    private val bridgeDelegate: BridgeDelegate<HotwireDestination>,
) : BridgeComponent<HotwireDestination>(name, bridgeDelegate) {
    companion object {
        val factory = BridgeComponentFactory("print", ::PrintComponent)
        private const val menuItemId = 2_024
    }

    private val fragment: HotwireFragment
        get() = bridgeDelegate.destination.fragment as HotwireFragment

    override fun onReceive(message: Message) {
        when (message.event) {
            "connect" -> addToolbarButton()
            "disconnect" -> removeToolbarButton()
        }
    }

    private fun addToolbarButton() {
        val toolbar = toolbar() ?: return
        removeToolbarButton()

        toolbar.menu.add(0, menuItemId, Menu.NONE, "Print").apply {
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            setOnMenuItemClickListener {
                printCurrentPage()
                true
            }
        }
    }

    private fun removeToolbarButton() {
        toolbar()?.menu?.removeItem(menuItemId)
    }

    private fun printCurrentPage() {
        val webView = bridgeDelegate.destination.navigator.session.webView
        val printAdapter = webView.createPrintDocumentAdapter("Cluster Headache Tracker")
        val printManager = fragment.requireContext().getSystemService(PrintManager::class.java)
        printManager?.print("Cluster Headache Tracker", printAdapter, null)
    }

    private fun toolbar(): MaterialToolbar? {
        return fragment.view?.findViewById(dev.hotwire.navigation.R.id.toolbar)
    }
}
