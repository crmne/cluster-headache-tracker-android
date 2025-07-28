package me.paolino.clusterheadachetracker.bridge

import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dev.hotwire.core.bridge.BridgeComponent
import dev.hotwire.core.bridge.BridgeDelegate
import dev.hotwire.navigation.destinations.HotwireDestination
import dev.hotwire.core.bridge.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.paolino.clusterheadachetracker.util.AuthEvents

class ButtonComponent(
    name: String,
    private val delegate: BridgeDelegate<HotwireDestination>
) : BridgeComponent<HotwireDestination>(name, delegate) {
    
    private val fragment: Fragment
        get() = delegate.destination.fragment
    
    private val activity: AppCompatActivity?
        get() = fragment.activity as? AppCompatActivity
    
    private var currentButtonTitle: String? = null
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
        Log.d(TAG, "Received button connect: ${data.title}")
        
        currentButtonTitle = data.title
        
        // Remove any existing menu provider
        menuProvider?.let { provider ->
            activity?.removeMenuProvider(provider)
        }
        
        // Create and add new menu provider
        menuProvider = object : MenuProvider {
            override fun onCreateMenu(menu: android.view.Menu, menuInflater: android.view.MenuInflater) {
                currentButtonTitle?.let { title ->
                    val menuItem = menu.add(0, android.view.Menu.FIRST, android.view.Menu.NONE, title)
                    menuItem.setShowAsAction(android.view.MenuItem.SHOW_AS_ACTION_ALWAYS)
                }
            }
            
            override fun onMenuItemSelected(menuItem: android.view.MenuItem): Boolean {
                return if (menuItem.itemId == android.view.Menu.FIRST) {
                    handleButtonClick()
                    true
                } else {
                    false
                }
            }
        }
        
        activity?.addMenuProvider(menuProvider!!, fragment.viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun handleDisconnect() {
        Log.d(TAG, "Button disconnected")
        currentButtonTitle = null
        
        // Remove menu provider when disconnecting
        menuProvider?.let { provider ->
            activity?.removeMenuProvider(provider)
        }
        menuProvider = null
    }
    
    private fun handleButtonClick() {
        Log.d(TAG, "Button clicked: $currentButtonTitle")
        
        // Reply to the connect event when button is clicked
        replyTo("connect")
        
        // Check if this is a sign out button
        if (currentButtonTitle?.contains("Sign Out", ignoreCase = true) == true) {
            handleSignOut()
        }
    }
    
    private fun handleSignOut() {
        // After a delay, trigger the sign out flow
        CoroutineScope(Dispatchers.Main).launch {
            delay(500)
            val intent = Intent(AuthEvents.SIGN_OUT_REQUESTED)
            LocalBroadcastManager.getInstance(fragment.requireContext())
                .sendBroadcast(intent)
        }
    }
    

    @Serializable
    data class MessageData(
        @SerialName("title") val title: String,
        @SerialName("iosImage") val iosImage: String? = null,
        @SerialName("androidImage") val androidImage: String? = null
    )

    companion object {
        const val TAG = "ButtonComponent"
    }
}