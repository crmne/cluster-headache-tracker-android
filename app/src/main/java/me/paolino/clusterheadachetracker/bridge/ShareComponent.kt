package me.paolino.clusterheadachetracker.bridge

import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import dev.hotwire.core.bridge.BridgeComponent
import dev.hotwire.core.bridge.BridgeDelegate
import dev.hotwire.navigation.destinations.HotwireDestination
import dev.hotwire.core.bridge.Message
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class ShareComponent(
    name: String,
    private val delegate: BridgeDelegate<HotwireDestination>
) : BridgeComponent<HotwireDestination>(name, delegate) {

    private val fragment: Fragment
        get() = delegate.destination.fragment
    
    private val activity: AppCompatActivity?
        get() = fragment.activity as? AppCompatActivity
    
    private var shareUrl: String? = null
    private var menuProvider: MenuProvider? = null

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
        
        // Remove any existing menu provider
        menuProvider?.let { provider ->
            activity?.removeMenuProvider(provider)
        }
        
        // Create and add new menu provider
        menuProvider = object : MenuProvider {
            override fun onCreateMenu(menu: android.view.Menu, menuInflater: android.view.MenuInflater) {
                if (shareUrl != null) {
                    val menuItem = menu.add(0, android.view.Menu.FIRST + 1, android.view.Menu.NONE, "Share")
                    menuItem.setShowAsAction(android.view.MenuItem.SHOW_AS_ACTION_ALWAYS)
                }
            }
            
            override fun onMenuItemSelected(menuItem: android.view.MenuItem): Boolean {
                return if (menuItem.itemId == android.view.Menu.FIRST + 1) {
                    performShare()
                    true
                } else {
                    false
                }
            }
        }
        
        activity?.addMenuProvider(menuProvider!!, fragment.viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun handleDisconnect() {
        Log.d(TAG, "Share disconnected")
        shareUrl = null
        
        // Remove menu provider when disconnecting
        menuProvider?.let { provider ->
            activity?.removeMenuProvider(provider)
        }
        menuProvider = null
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
        } catch (e: Exception) {
            Log.e(TAG, "Failed to share", e)
        }
    }
    

    @Serializable
    data class MessageData(
        @SerialName("url") val url: String
    )
    
    companion object {
        const val TAG = "ShareComponent"
    }
}