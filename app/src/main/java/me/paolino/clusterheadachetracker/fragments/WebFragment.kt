package me.paolino.clusterheadachetracker.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dev.hotwire.core.bridge.BridgeComponent
import dev.hotwire.core.turbo.errors.HttpError
import dev.hotwire.core.turbo.errors.VisitError
import dev.hotwire.navigation.destinations.HotwireDestinationDeepLink
import dev.hotwire.navigation.fragments.HotwireWebFragment
import dev.hotwire.core.turbo.visit.VisitOptions
import kotlinx.coroutines.launch
import me.paolino.clusterheadachetracker.MainActivity
import me.paolino.clusterheadachetracker.bridge.ButtonComponent
import me.paolino.clusterheadachetracker.bridge.ShareComponent
import me.paolino.clusterheadachetracker.util.AuthEvents
import me.paolino.clusterheadachetracker.util.SIGN_IN_URL

@HotwireDestinationDeepLink(uri = "hotwire://fragment/web")
class WebFragment : HotwireWebFragment() {
    
    companion object {
        const val TAG = "WebFragment"
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG, "Creating view for location: $location")
        return super.onCreateView(inflater, container, savedInstanceState)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Bridge components will handle their own menu items through the activity
    }
    
    override fun onVisitCompleted(location: String, completedOffline: Boolean) {
        super.onVisitCompleted(location, completedOffline)
        
        // Check if we just completed a visit from sign_in (indicating successful auth)
        if (navigator.previousLocation?.contains("/sign_in") == true && !location.contains("/sign_in")) {
            Log.d(TAG, "Authentication successful, sending authentication changed event")
            val intent = Intent(AuthEvents.AUTHENTICATION_CHANGED)
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
        }
        
    }
    
    override fun onVisitErrorReceived(location: String, error: VisitError) {
        when (error) {
            is HttpError.ClientError.Unauthorized -> {
                Log.d(TAG, "Received 401 unauthorized error at: $location")
                handleUnauthorizedError()
            }
            else -> super.onVisitErrorReceived(location, error)
        }
    }
    
    // Bridge components are now managed differently in Hotwire Native 1.1
    // They register and unregister automatically through the BridgeDelegate
    
    // Menu handling is now done directly by bridge components
    // They manage their own menu items through the bridge delegate
    
    private fun handleUnauthorizedError() {
        lifecycleScope.launch {
            // Navigate to sign in as a modal
            navigator.route(
                location = SIGN_IN_URL,
                options = VisitOptions(),
                bundle = Bundle().apply {
                    putString("presentation", "modal")
                }
            )
        }
    }
    
}