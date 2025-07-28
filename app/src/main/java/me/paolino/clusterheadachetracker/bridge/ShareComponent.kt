package me.paolino.clusterheadachetracker.bridge

import android.content.Intent
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import dev.hotwire.core.bridge.BridgeComponent
import dev.hotwire.core.bridge.BridgeDelegate
import dev.hotwire.core.bridge.Message
import dev.hotwire.navigation.destinations.HotwireDestination
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.paolino.clusterheadachetracker.R

class ShareComponent(name: String, private val delegate: BridgeDelegate<HotwireDestination>) :
    BridgeComponent<HotwireDestination>(name, delegate) {

    private val fragment: Fragment
        get() = delegate.destination.fragment

    private val activity: AppCompatActivity?
        get() = fragment.activity as? AppCompatActivity

    private var shareUrl: String? = null
    private var currentMessage: Message? = null
    private var menuItem: MenuItem? = null

    init {
        // Remove any existing menu items when component is created
        cleanupMenuItems()
    }

    override fun onReceive(message: Message) {
        when (message.event) {
            "connect" -> handleConnect(message)
            "disconnect" -> handleDisconnect()
            else -> Log.w(TAG, "Unknown event: ${message.event}")
        }
    }

    private fun handleConnect(message: Message) {
        val data = message.data<MessageData>() ?: return
        Log.d(TAG, "Received share connect: url=${data.url}")

        shareUrl = data.url
        currentMessage = message

        // Clean up any existing menu items first
        cleanupMenuItems()

        // Add share menu item to the toolbar
        fragment.view?.let { view ->
            // Get the fragment's toolbar
            val toolbar = view.findViewById<com.google.android.material.appbar.MaterialToolbar>(
                dev.hotwire.navigation.R.id.toolbar,
            )
            val menu = toolbar?.menu

            menu?.let { m ->
                // Add share menu item
                val newItem = m.add(0, Menu.FIRST + SHARE_MENU_ID_OFFSET, Menu.NONE, "Share")
                newItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)

                // Add share icon
                val shareIcon = fragment.context?.getDrawable(android.R.drawable.ic_menu_share)
                newItem.icon = shareIcon

                // Set click listener
                newItem.setOnMenuItemClickListener {
                    performShare()
                    true
                }

                menuItem = newItem
            }
        }
    }

    private fun handleDisconnect() {
        Log.d(TAG, "Share disconnected")
        shareUrl = null
        currentMessage = null

        // Remove menu item
        cleanupMenuItems()
    }

    private fun cleanupMenuItems() {
        fragment.view?.let { view ->
            val toolbar = view.findViewById<com.google.android.material.appbar.MaterialToolbar>(
                dev.hotwire.navigation.R.id.toolbar,
            )
            toolbar?.menu?.let { menu ->
                // Remove our menu item
                menuItem?.let { item ->
                    menu.removeItem(item.itemId)
                }
            }
        }
        menuItem = null
    }

    private fun performShare() {
        val url = shareUrl ?: return

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, url)
        }

        try {
            fragment.startActivity(Intent.createChooser(shareIntent, "Share via"))
            // Reply to the connect event to signal completion
            replyTo("connect")
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            Log.e(TAG, "Failed to share", e)
        }
    }

    @Serializable
    data class MessageData(@SerialName("url") val url: String)

    companion object {
        const val TAG = "ShareComponent"
        private const val SHARE_MENU_ID_OFFSET = 100
    }
}
