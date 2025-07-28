package me.paolino.clusterheadachetracker.bridge

import android.content.Intent
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dev.hotwire.core.bridge.BridgeComponent
import dev.hotwire.core.bridge.BridgeDelegate
import dev.hotwire.core.bridge.Message
import dev.hotwire.navigation.destinations.HotwireDestination
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.paolino.clusterheadachetracker.R
import me.paolino.clusterheadachetracker.util.AuthEvents

class ButtonComponent(name: String, private val delegate: BridgeDelegate<HotwireDestination>) :
    BridgeComponent<HotwireDestination>(name, delegate) {

    companion object {
        private const val TAG = "ButtonComponent"
        private const val SIGN_OUT_DELAY_MS = 500L
    }

    private val fragment: Fragment
        get() = delegate.destination.fragment

    private val activity: AppCompatActivity?
        get() = fragment.activity as? AppCompatActivity

    private var currentButtonTitle: String? = null
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
        Log.d(TAG, "Received button connect: ${data.title}")

        currentButtonTitle = data.title
        currentMessage = message

        // Clean up any existing menu items first
        cleanupMenuItems()

        // Add menu item to the toolbar
        fragment.view?.let { view ->
            // Get the fragment's toolbar
            val toolbar = view.findViewById<com.google.android.material.appbar.MaterialToolbar>(
                dev.hotwire.navigation.R.id.toolbar,
            )
            val menu = toolbar?.menu

            menu?.let { m ->
                // Add new menu item
                val newItem = m.add(0, Menu.FIRST, Menu.NONE, data.title)
                newItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)

                // Handle icon if provided
                data.androidImage?.let { imageName ->
                    val resourceId = fragment.resources.getIdentifier(
                        imageName,
                        "drawable",
                        fragment.requireContext().packageName,
                    )
                    if (resourceId != 0) {
                        newItem.setIcon(resourceId)
                    }
                }

                // Set click listener
                newItem.setOnMenuItemClickListener {
                    handleButtonClick()
                    true
                }

                menuItem = newItem
            }
        }
    }

    private fun handleDisconnect() {
        Log.d(TAG, "Button disconnected")
        currentButtonTitle = null
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

    private fun handleButtonClick() {
        Log.d(TAG, "Button clicked: $currentButtonTitle")

        // Reply to the web page
        currentMessage?.let { message ->
            replyTo(message.event)
        }

        // Check if this is a sign out button
        if (currentButtonTitle?.contains("Sign Out", ignoreCase = true) == true) {
            handleSignOut()
        }
    }

    private fun handleSignOut() {
        // After a delay, trigger the sign out flow
        CoroutineScope(Dispatchers.Main).launch {
            delay(SIGN_OUT_DELAY_MS)
            val intent = Intent(AuthEvents.SIGN_OUT_REQUESTED)
            LocalBroadcastManager.getInstance(fragment.requireContext())
                .sendBroadcast(intent)
        }
    }

    @Serializable
    data class MessageData(
        @SerialName("title") val title: String,
        @SerialName("iosImage") val iosImage: String? = null,
        @SerialName("androidImage") val androidImage: String? = null,
    )
}
