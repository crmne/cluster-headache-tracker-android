package me.paolino.clusterheadachetracker.fragments

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dev.hotwire.navigation.destinations.HotwireDestinationDeepLink
import dev.hotwire.navigation.fragments.HotwireWebBottomSheetFragment
import me.paolino.clusterheadachetracker.R
import android.webkit.WebSettings
import me.paolino.clusterheadachetracker.BuildConfig
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.content.Intent
import me.paolino.clusterheadachetracker.util.AuthEvents

@HotwireDestinationDeepLink(uri = "hotwire://fragment/web/modal")
class WebModalFragment : HotwireWebBottomSheetFragment() {
    
    companion object {
        const val TAG = "WebModalFragment"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_ClusterHeadacheTracker_BottomSheetModal)
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                // Set the bottom sheet to expanded state immediately
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
                
                // Make the bottom sheet full height but respect status bar
                behavior.peekHeight = 0
                behavior.isFitToContents = false
                behavior.expandedOffset = getStatusBarHeight()
                
                // Handle window insets
                ViewCompat.setOnApplyWindowInsetsListener(it) { view, insets ->
                    val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                    view.updatePadding(top = systemBarsInsets.top)
                    insets
                }
            }
        }
        
        // Configure window to not overlap status bar
        dialog.window?.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            // Make status bar content dark (for light backgrounds)
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        
        return dialog
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "Modal created for location: $location")
        
        // Delay WebView configuration to ensure it's fully initialized
        view.post {
            configureWebView()
        }
    }
    
    private fun configureWebView() {
        try {
            // Configure WebView settings for form submission
            navigator.session.webView.apply {
                settings.apply {
                    // Enable form data and autofill
                    saveFormData = true
                    domStorageEnabled = true
                    
                    // Ensure JavaScript is enabled (should be by default with Hotwire)
                    javaScriptEnabled = true
                    
                    // Allow mixed content for development
                    if (BuildConfig.DEBUG) {
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error configuring WebView", e)
        }
    }
    
    private fun getStatusBarHeight(): Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else {
            // Default to 24dp if can't find system resource
            (24 * resources.displayMetrics.density).toInt()
        }
    }
    
    
}