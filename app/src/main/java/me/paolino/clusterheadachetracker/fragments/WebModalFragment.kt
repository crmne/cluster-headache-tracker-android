package me.paolino.clusterheadachetracker.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import dev.hotwire.navigation.destinations.HotwireDestinationDeepLink
import dev.hotwire.navigation.fragments.HotwireWebBottomSheetFragment

@HotwireDestinationDeepLink(uri = "hotwire://fragment/web/modal")
class WebModalFragment : HotwireWebBottomSheetFragment() {
    
    companion object {
        const val TAG = "WebModalFragment"
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "Modal created for location: $location")
    }
}